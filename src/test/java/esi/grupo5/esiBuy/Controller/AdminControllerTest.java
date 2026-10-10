package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Service.AdminService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean private UserService userService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private JwtService jwtService;

    @Test
    void crearAdministrador_datosValidosDevuelve201YRespuestaPublica() throws Exception {
        AdministradorResponseDTO response = AdministradorResponseDTO.builder()
                .id("admin-1")
                .name("Ana")
                .apellidos("Pérez")
                .email("ana@esibuy.com")
                .sede("Madrid")
                .rol(esi.grupo5.esiBuy.Model.enums.Rol.ADMINISTRADOR)
                .mensaje("Administrador creado correctamente")
                .build();
        when(adminService.crearAdministrador(any(AdministradorRegistroDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administradorDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@esibuy.com"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.sede").value("Madrid"))
                .andExpect(jsonPath("$.mensaje").value("Administrador creado correctamente"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        verify(adminService).crearAdministrador(any(AdministradorRegistroDTO.class));
    }

    @Test
    void crearAdministrador_emailInvalidoSeRechazaAntesDelServicio() throws Exception {
        AdministradorRegistroDTO dto = new AdministradorRegistroDTO(
                "Ana", "Pérez", "email-invalido", "Clave#2026x", "Madrid");

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).crearAdministrador(any());
    }

    @Test
    void crearAdministrador_contrasenaDebilSeRechazaAntesDelServicio() throws Exception {
        AdministradorRegistroDTO dto = new AdministradorRegistroDTO(
                "Ana", "Pérez", "ana@esibuy.com", "weak", "Madrid");

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(adminService, never()).crearAdministrador(any());
    }

    private AdministradorRegistroDTO administradorDTO() {
        return new AdministradorRegistroDTO(
                "Ana", "Pérez", "ana@esibuy.com", "Clave#2026x", "Madrid");
    }
}
