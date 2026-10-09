package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
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
        verify(cookieUtil).setTokenCookies(any(), eq(response));
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
}
