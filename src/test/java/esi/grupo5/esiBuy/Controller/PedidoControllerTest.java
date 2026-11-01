package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Dto.PedidoResponseDTO;
import esi.grupo5.esiBuy.Model.EstadoPedido;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.PedidoService;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void crearPedido_validaSolicitudYDevuelveCreado() throws Exception {
        when(pedidoService.crearPedido(
                org.mockito.ArgumentMatchers.eq("cliente-1"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PedidoResponseDTO(
                        "pedido-1",
                        List.of(),
                        new DireccionEnvioDTO(
                                "Ana García", "Calle Mayor 1", "Madrid", "Madrid", "28001", "España",
                                "+34600000000"),
                        "pago-1",
                        TipoMetodoPago.TARJETA,
                        "Personal",
                        "**** 1234",
                        2_500,
                        EstadoPedido.RECIBIDO,
                        null));

        mockMvc.perform(post("/pedidos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType("application/json")
                        .content("""
                                {
                                  "metodoPagoId": "pago-1",
                                  "direccionEnvio": {
                                    "nombreCompleto": "Ana García",
                                    "direccion": "Calle Mayor 1",
                                    "ciudad": "Madrid",
                                    "provincia": "Madrid",
                                    "codigoPostal": "28001",
                                    "pais": "España",
                                    "telefono": "+34600000000"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pedido-1"))
                .andExpect(jsonPath("$.totalCent").value(2_500))
                .andExpect(jsonPath("$.estado").value("RECIBIDO"));

        verify(pedidoService).crearPedido(
                org.mockito.ArgumentMatchers.eq("cliente-1"),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void crearPedido_rechazaDireccionIncompleta() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType("application/json")
                        .content("""
                                {
                                  "metodoPagoId": "pago-1",
                                  "direccionEnvio": {
                                    "nombreCompleto": "",
                                    "direccion": "",
                                    "ciudad": "",
                                    "provincia": "",
                                    "codigoPostal": "",
                                    "pais": "",
                                    "telefono": ""
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPedidos_devuelvePedidosDelClienteAutenticado() throws Exception {
        when(pedidoService.listarPedidos("cliente-1")).thenReturn(List.of());

        mockMvc.perform(get("/pedidos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(pedidoService).listarPedidos("cliente-1");
    }
}
