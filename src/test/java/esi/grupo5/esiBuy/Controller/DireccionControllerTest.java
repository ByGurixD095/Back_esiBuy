package esi.grupo5.esiBuy.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import esi.grupo5.esiBuy.Config.GlobalExceptionHandler;
import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Dto.DireccionGuardadaResponseDTO;
import esi.grupo5.esiBuy.Service.DireccionService;

@ExtendWith(MockitoExtension.class)
class DireccionControllerTest {

    @Mock
    private DireccionService direccionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DireccionController(direccionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarDevuelveSoloRespuestaDelClienteAutenticado() throws Exception {
        when(direccionService.listar("cliente-1")).thenReturn(List.of(direccion()));

        mockMvc.perform(get("/users/me/direcciones")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("direccion-1"))
                .andExpect(jsonPath("$[0].ciudad").value("Madrid"));
    }

    @Test
    void crearDevuelve201() throws Exception {
        when(direccionService.crear(eq("cliente-1"), any(DireccionEnvioDTO.class)))
                .thenReturn(direccion());

        mockMvc.perform(post("/users/me/direcciones")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("direccion-1"));
    }

    @Test
    void actualizarYEliminarUsanLaIdentidadAutenticada() throws Exception {
        when(direccionService.actualizar(eq("cliente-1"), eq("direccion-1"), any(DireccionEnvioDTO.class)))
                .thenReturn(direccion());

        mockMvc.perform(put("/users/me/direcciones/direccion-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("direccion-1"));

        mockMvc.perform(delete("/users/me/direcciones/direccion-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNoContent());

        verify(direccionService).eliminar("cliente-1", "direccion-1");
    }

    private DireccionGuardadaResponseDTO direccion() {
        return new DireccionGuardadaResponseDTO(
                "direccion-1", "Ana García", "Calle Mayor 1", "Madrid",
                "Madrid", "28001", "España", "+34600000000");
    }

    private String cuerpo() {
        return """
                {
                  "nombreCompleto": "Ana García",
                  "direccion": "Calle Mayor 1",
                  "ciudad": "Madrid",
                  "provincia": "Madrid",
                  "codigoPostal": "28001",
                  "pais": "España",
                  "telefono": "+34600000000"
                }
                """;
    }
}
