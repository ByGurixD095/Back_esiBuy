package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.MetodoPagoController;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.MetodoPagoService;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;

@WebMvcTest(MetodoPagoController.class)
@AutoConfigureMockMvc(addFilters = false)
class MetodoPagoControllerModificarTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetodoPagoService metodoPagoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void modificarMetodoPago_datosValidos_devuelve200() throws Exception {

        MetodoPagoResponseDTO respuesta = new MetodoPagoResponseDTO(
                "metodo-1",
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234",
                "Ines Granja",
                "12/28"
        );

        when(metodoPagoService.modificarMetodoPago(
                org.mockito.ArgumentMatchers.eq("cliente-1"),
                org.mockito.ArgumentMatchers.eq("metodo-1"),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(respuesta);

        mockMvc.perform(put("/users/me/metodoPago/metodo-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipo": "TARJETA",
                                    "alias": "Mi tarjeta",
                                    "numeroTarjeta": "1234567890121234",
                                    "titular": "Ines Granja",
                                    "caducidad": "12/28"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("metodo-1"))
                .andExpect(jsonPath("$.tipo").value("TARJETA"))
                .andExpect(jsonPath("$.alias").value("Mi tarjeta"))
                .andExpect(jsonPath("$.referencia").value("**** 1234"));

        verify(metodoPagoService).modificarMetodoPago(
                org.mockito.ArgumentMatchers.eq("cliente-1"),
                org.mockito.ArgumentMatchers.eq("metodo-1"),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void modificarMetodoPago_datosInvalidos_devuelve400() throws Exception {

        mockMvc.perform(put("/users/me/metodoPago/metodo-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tipo": "TARJETA",
                                    "alias": "Mi tarjeta",
                                    "numeroTarjeta": "123",
                                    "titular": "Ines Granja",
                                    "caducidad": "12/28"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(metodoPagoService);
    }
}