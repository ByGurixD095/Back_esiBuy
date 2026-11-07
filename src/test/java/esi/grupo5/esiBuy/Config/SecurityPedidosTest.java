package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import esi.grupo5.esiBuy.Controller.PedidoController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.PedidoService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityPedidosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PedidoService pedidoService;

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
    void cliente_puedeConsultarSusPedidos() throws Exception {
        simularLogin("CLIENTE");
        when(pedidoService.listarPedidos("cliente-1")).thenReturn(List.of());

        mockMvc.perform(get("/pedidos").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedor_noPuedeConsultarPedidos() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(get("/pedidos").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void vendedor_puedeConsultarSuColaDePedidos() throws Exception {
        simularLogin("VENDEDOR");
        when(pedidoService.listarPedidosVendedor("cliente-1", false)).thenReturn(List.of());

        mockMvc.perform(get("/pedidos/vendedor").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedor_puedeMarcarSuPedidoComoEnviado() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(patch("/pedidos/{pedidoId}/enviar", "pedido-1")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_noPuedeMarcarPedidoComoEnviado() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(patch("/pedidos/{pedidoId}/enviar", "pedido-1")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void cliente_noPuedeConsultarLaColaDePedidosDelVendedor() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(get("/pedidos/vendedor").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void sinAutenticacion_noPuedeConsultarPedidos() throws Exception {
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }
}
