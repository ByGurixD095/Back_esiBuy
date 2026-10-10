package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ExpiredTokenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.PasswordExpiredException;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserController controller;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private CookieUtil cookieUtil;
    @MockitoBean private JwtService jwtService;

    @Test
    void login_delegaConLaIpYDevuelveLaRespuestaConCookies() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(
                "access-token", "refresh-token", "CLIENTE", "NORMAL", "SUCCESS", "cliente@test.com");
        when(authService.login(any(LoginRequestDTO.class), eq("127.0.0.1"))).thenReturn(response);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequestDTO("cliente@test.com", "Password#1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.mfaStatus").value("SUCCESS"));

        verify(authService).login(any(LoginRequestDTO.class), eq("127.0.0.1"));
        verify(cookieUtil).setTokenCookiesIfPresent(any(), eq(response));
    }

    @Test
    void refresh_leeRefreshCookieYDevuelveTokensActualizados() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(
                "new-access-token", "refresh-token", "CLIENTE", "NORMAL", "SUCCESS", "cliente@test.com");
        when(authService.refreshToken("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/users/refresh").cookie(new jakarta.servlet.http.Cookie(
                        "refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refreshToken("refresh-token");
        verify(cookieUtil).setTokenCookies(any(), eq(response));
    }

    @Test
    void recoverPassword_delegaLaSolicitudYDevuelveOk() throws Exception {
        PasswordResetRequestDTO request = new PasswordResetRequestDTO("cliente@test.com");

        mockMvc.perform(post("/users/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).requestPasswordReset(request);
    }

    @Test
    void resetPassword_delegaLaConfirmacionYDevuelveOk() throws Exception {
        PasswordResetConfirmDTO request =
                new PasswordResetConfirmDTO("token", "NuevaClave1!", "NuevaClave1!");

        mockMvc.perform(post("/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).resetPassword(request);
    }

    @Test
    void controllerMe_logoutYModificacionDePerfil_deleganAlServicio() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user-1");
        UsuarioResponseDTO user = UsuarioResponseDTO.usuarioBuilder().id("user-1").build();
        when(userService.getUserById("user-1")).thenReturn(user);

        assertEquals(user, controller.getUserById(authentication).getBody());
        assertEquals(HttpStatus.NO_CONTENT,
                controller.modificarMiPerfil(new UserSelfUpdateDTO(
                        "Ana", null, null, null, null, null, null), authentication).getStatusCode());
        assertEquals(HttpStatus.OK, controller.logout(mock(HttpServletResponse.class)).getStatusCode());

        verify(userService).getUserById("user-1");
        verify(userService).modificarMiPerfil(eq("user-1"), any(UserSelfUpdateDTO.class));
        verify(cookieUtil).clearTokenCookies(any());
    }

    @Test
    void registroDeClienteYVendedor_soloCreanCookieSiHayToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        LoginResponseDTO withToken = new LoginResponseDTO("access", "refresh", "CLIENTE", "NORMAL");
        LoginResponseDTO withoutToken = new LoginResponseDTO(null, null, "CLIENTE", "NORMAL");
        ClienteRegistroDTO cliente = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", null, null,
                "12345678A", java.time.LocalDate.of(2000, 1, 1), null);
        VendedorRegisterRequest vendedor = new VendedorRegisterRequest(
                "Luis", "García", "luis@test.com", "Strong#2026", null, null,
                "Tienda", "B12345678", "electronica");
        when(userService.registrarCliente(cliente)).thenReturn(withToken);
        when(userService.registrarVendedor(vendedor)).thenReturn(withoutToken);

        assertEquals(HttpStatus.CREATED, controller.registrarCliente(cliente, response).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.registerVendedor(vendedor, response).getStatusCode());

        verify(cookieUtil).setTokenCookiesIfPresent(response, withToken);
        verify(cookieUtil).setTokenCookiesIfPresent(response, withoutToken);
    }

    @Test
    void loginSinAccessToken_noEscribeCookiesYErroresDeNegocioSeConservan() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authService.login(any(LoginRequestDTO.class), eq("127.0.0.1")))
                .thenReturn(new LoginResponseDTO(null, null, "CLIENTE", "NORMAL"));

        controller.login(new LoginRequestDTO("cliente@test.com", "secret"), response, request);
        verify(cookieUtil).setTokenCookiesIfPresent(eq(response), any());

        when(authService.login(any(LoginRequestDTO.class), any()))
                .thenThrow(new BusinessException("backend error", 503, "SERVICE_UNAVAILABLE"));
        BusinessException propagated = assertThrows(BusinessException.class,
                () -> controller.login(new LoginRequestDTO("cliente@test.com", "secret"), response, request));
        assertEquals(503, propagated.getHttpStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", propagated.getErrorCode());
    }

    @Test
    void refreshSinCookieYErroresDeNegocio_seGestionan() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertThrows(esi.grupo5.esiBuy.Exception.ValidationException.class,
                () -> controller.refresh(" ", response));
        when(authService.refreshToken("refresh")).thenThrow(
                new BusinessException("backend error", 503, "SERVICE_UNAVAILABLE"));

        BusinessException propagated = assertThrows(BusinessException.class,
                () -> controller.refresh("refresh", response));
        assertEquals(503, propagated.getHttpStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", propagated.getErrorCode());
    }

    @Test
    void modificacionDePerfil_conservaLaExcepcionDeNegocio() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user-1");
        doThrow(new BusinessException("backend error", 500, "INTERNAL_ERROR"))
                .when(userService).modificarMiPerfil(eq("user-1"), any(UserSelfUpdateDTO.class));

        BusinessException propagated = assertThrows(BusinessException.class,
                () -> controller.modificarMiPerfil(new UserSelfUpdateDTO(
                        null, null, null, null, null, null, null), authentication));
        assertEquals("INTERNAL_ERROR", propagated.getErrorCode());
    }

    @Test
    void loginYRefresh_conservanErroresDeAutenticacion() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authService.login(any(LoginRequestDTO.class), any()))
                .thenThrow(new AuthException("invalid credentials"))
                .thenThrow(new PasswordExpiredException("password expired"));

        assertThrows(AuthException.class, () -> controller.login(
                new LoginRequestDTO("user@test.com", "bad"), response, request));
        assertThrows(PasswordExpiredException.class, () -> controller.login(
                new LoginRequestDTO("user@test.com", "expired"), response, request));

        when(authService.refreshToken("expired")).thenThrow(new ExpiredTokenException("expired"));
        assertThrows(ExpiredTokenException.class, () -> controller.refresh("expired", response));
    }

    @Test
    void registroConservaErroresDeValidacionYReenvuelveErroresInternos() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ClienteRegistroDTO cliente = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", null, null,
                "12345678A", java.time.LocalDate.of(2000, 1, 1), null);
        VendedorRegisterRequest vendedor = new VendedorRegisterRequest(
                "Luis", "García", "luis@test.com", "Strong#2026", null, null,
                "Tienda", "B12345678", "electronica");

        when(userService.registrarCliente(cliente)).thenThrow(new ConflictException("email exists"));
        assertThrows(ConflictException.class, () -> controller.registrarCliente(cliente, response));

        when(userService.registrarVendedor(vendedor))
                .thenThrow(new BusinessException("database unavailable", 500, "INTERNAL_ERROR"));
        BusinessException propagated = assertThrows(BusinessException.class,
                () -> controller.registerVendedor(vendedor, response));
        assertEquals("INTERNAL_ERROR", propagated.getErrorCode());
        assertEquals(500, propagated.getHttpStatusCode());
    }

    @Test
    void modificarPerfil_conservaNotFoundYConflictos() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user-1");
        UserSelfUpdateDTO update = new UserSelfUpdateDTO(
                null, null, null, null, null, null, null);
        doThrow(new NotFoundException("not found"))
                .when(userService).modificarMiPerfil("user-1", update);
        assertThrows(NotFoundException.class, () -> controller.modificarMiPerfil(update, authentication));

        doThrow(new ConflictException("conflict"))
                .when(userService).modificarMiPerfil("user-1", update);
        assertThrows(ConflictException.class, () -> controller.modificarMiPerfil(update, authentication));
    }
}
