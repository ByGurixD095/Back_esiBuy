package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Config.JwtAuthenticationFilter;
import esi.grupo5.esiBuy.Config.SecurityConfig;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerEliminarCuentaTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private CookieUtil cookieUtil;
    @MockitoBean private UsuarioRepository usuarioRepository;

    @BeforeEach
    void clearSecurityContextBeforeTest() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContextAfterTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clienteAutenticadoPuedeEliminarSuCuenta() throws Exception {
        simularSesion("CLIENTE", "cliente-1");

        mockMvc.perform(delete("/users/me")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());

        verify(userService).eliminarCuenta("cliente-1");
        verify(cookieUtil).clearTokenCookies(any());
    }

    @Test
    void vendedorAutenticadoPuedeEliminarSuCuenta() throws Exception {
        simularSesion("VENDEDOR", "vendedor-1");

        mockMvc.perform(delete("/users/me")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());

        verify(userService).eliminarCuenta("vendedor-1");
    }

    @Test
    void sinSesionNoPuedeEliminarCuenta() throws Exception {
        mockMvc.perform(delete("/users/me"))
                .andExpect(status().is4xxClientError());

        verify(userService, never()).eliminarCuenta(any());
    }

    @Test
    void cuentaEliminadaNoPuedeReutilizarSuTokenDeAcceso() throws Exception {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("cliente-eliminado");
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalseAndEliminadoFalse("cliente-eliminado"))
                .thenReturn(false);

        mockMvc.perform(delete("/users/me")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).eliminarCuenta(any());
    }

    private void simularSesion(String role, String id) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn(id);
        when(jwtService.extractRol(any())).thenReturn(role);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalseAndEliminadoFalse(id))
                .thenReturn(true);
    }
}
