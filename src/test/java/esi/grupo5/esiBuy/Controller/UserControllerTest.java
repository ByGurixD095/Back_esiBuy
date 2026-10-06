package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, jwtService)).build();
        lenient().when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900);
        lenient().when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(86_400);
    }

    @Test
    void loginDevuelveRespuestaYConfiguraCookies() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(
                "access-token", "refresh-token", "CLIENTE", "NORMAL");
        when(userService.login(any(), eq("127.0.0.1"))).thenReturn(response);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"cliente@test.com","password":"correcta"}
                                """)
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.tipoCliente").value("NORMAL"))
                .andExpect(cookie().value("accessToken", "access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"));

        verify(userService).login(any(), eq("127.0.0.1"));
    }

    @Test
    void refreshDevuelveNuevoAccessTokenYConfiguraCookies() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(
                "new-access-token", "refresh-token", "CLIENTE", "NORMAL");
        when(userService.refreshToken("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/users/refresh").cookie(
                        new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(cookie().value("accessToken", "new-access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"));

        verify(userService).refreshToken("refresh-token");
    }

    @Test
    void recoverPasswordDelegaLaSolicitudYDevuelveOk() throws Exception {
        mockMvc.perform(post("/users/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@test.com\"}"))
                .andExpect(status().isOk());

        verify(userService).requestPasswordReset(any(PasswordResetRequestDTO.class));
    }

    @Test
    void resetPasswordDelegaLaConfirmacionYDevuelveOk() throws Exception {
        mockMvc.perform(post("/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"token-valido","pwd1":"NuevaClave1!","pwd2":"NuevaClave1!"}
                                """))
                .andExpect(status().isOk());

        verify(userService).resetPassword(any(PasswordResetConfirmDTO.class));
    }
}
