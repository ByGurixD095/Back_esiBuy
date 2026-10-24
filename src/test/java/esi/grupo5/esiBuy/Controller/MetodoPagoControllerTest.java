
package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Service.MetodoPagoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoControllerTest {

    @Mock
    private MetodoPagoService metodoPagoService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MetodoPagoController controller =
                new MetodoPagoController(metodoPagoService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void registrarMetodoPago_DatosValidos_Devuelve201() throws Exception {
        MetodoPagoResponseDTO respuesta = new MetodoPagoResponseDTO(
                "pago123",
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234");

        when(metodoPagoService.registrarMetodoPago(
                eq("cliente123"), any()))
                .thenReturn(respuesta);

        mockMvc.perform(post("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tipo": "TARJETA",
                          "alias": "Mi tarjeta",
                          "referencia": "**** 1234"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pago123"))
                .andExpect(jsonPath("$.tipo").value("TARJETA"))
                .andExpect(jsonPath("$.alias").value("Mi tarjeta"));
    }

    @Test
    void registrarMetodoPago_AliasVacio_Devuelve400() throws Exception {
        mockMvc.perform(post("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tipo": "TARJETA",
                          "alias": "",
                          "referencia": "**** 1234"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(metodoPagoService);
    }

    @Test
    void registrarMetodoPago_TipoNulo_Devuelve400() throws Exception {
        mockMvc.perform(post("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tipo": null,
                          "alias": "Mi tarjeta",
                          "referencia": "**** 1234"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(metodoPagoService);
    }

    @Test
    void registrarMetodoPago_ReferenciaVacia_Devuelve400() throws Exception {
        mockMvc.perform(post("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tipo": "TARJETA",
                          "alias": "Mi tarjeta",
                          "referencia": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(metodoPagoService);
    }

    @Test
    void listarMetodosPago_ClienteValido_Devuelve200() throws Exception {
        when(metodoPagoService.listarMetodosPago("cliente123"))
                .thenReturn(List.of(
                        new MetodoPagoResponseDTO(
                                "pago123",
                                TipoMetodoPago.TARJETA,
                                "Mi tarjeta",
                                "**** 1234")));

        mockMvc.perform(get("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("pago123"))
                .andExpect(jsonPath("$[0].alias").value("Mi tarjeta"));
    }
}
