package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.DireccionController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.DireccionService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(DireccionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityDireccionesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DireccionService direccionService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clientePuedeConsultarSusDirecciones() throws Exception {
        simularLogin("CLIENTE");
        when(direccionService.listar("cliente-1")).thenReturn(List.of());

        mockMvc.perform(get("/users/me/direcciones").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorNoPuedeConsultarDireccionesDeCliente() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(get("/users/me/direcciones").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(direccionService);
    }

    @Test
    void sinAutenticacionNoPuedeConsultarDirecciones() throws Exception {
        mockMvc.perform(get("/users/me/direcciones"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(direccionService);
    }

    private void simularLogin(String rol) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("cliente-1");
        when(jwtService.extractRol(any())).thenReturn(rol);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse("cliente-1"))
                .thenReturn(true);
    }
}
