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

import esi.grupo5.esiBuy.Controller.CarritoController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CarritoService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(CarritoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityCarritoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CarritoService carritoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void simularLogin(String rol) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("cliente-1");
        when(jwtService.extractRol(any())).thenReturn(rol);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse("cliente-1"))
                .thenReturn(true);
    }

    @Test
    void cliente_puedeConsultarCarrito() throws Exception {
        simularLogin("CLIENTE");
        when(carritoService.listarCarrito("cliente-1")).thenReturn(List.of());

        mockMvc.perform(get("/carrito").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedor_noPuedeConsultarCarrito() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(get("/carrito").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carritoService);
    }

    @Test
    void sinAutenticacion_noPuedeConsultarCarrito() throws Exception {
        mockMvc.perform(get("/carrito"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carritoService);
    }
}
