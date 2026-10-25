
package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Config.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import esi.grupo5.esiBuy.Exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        MetodoPagoController controller =
                new MetodoPagoController(metodoPagoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
                          "numeroTarjeta": "**** 1234",
                          "titular": "Ana Pérez",
                          "caducidad": "12/2028"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pago123"))
                .andExpect(jsonPath("$.tipo").value("TARJETA"))
                .andExpect(jsonPath("$.alias").value("Mi tarjeta"));
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
                          "numeroTarjeta": "**** 1234",
                          "titular": "Ana Pérez",
                          "caducidad": "12/2028"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(metodoPagoService);
    }

    @Test
    void registrarMetodoPago_NumeroTarjetaInvalido_Devuelve400() throws Exception {
        mockMvc.perform(post("/users/me/metodoPago")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tipo": "TARJETA",
                          "alias": "Mi tarjeta",
                          "numeroTarjeta": "no-es-una-tarjeta",
                          "titular": "Ana Pérez",
                          "caducidad": "12/2028"
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

    @Test
    void eliminarMetodoPago_ClienteValido_Devuelve204() throws Exception {
        mockMvc.perform(delete("/users/me/metodoPago/pago123")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isNoContent());

        verify(metodoPagoService)
                .eliminarMetodoPago("cliente123", "pago123");
    }

    @Test
    void eliminarMetodoPago_MetodoAjeno_ServiceLanzaNotFound()
            throws Exception {

        doThrow(new NotFoundException("Método de pago no encontrado"))
                .when(metodoPagoService)
                .eliminarMetodoPago("cliente123", "pagoAjeno");

        mockMvc.perform(delete("/users/me/metodoPago/pagoAjeno")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isNotFound());

        verify(metodoPagoService)
                .eliminarMetodoPago("cliente123", "pagoAjeno");
    }
}
