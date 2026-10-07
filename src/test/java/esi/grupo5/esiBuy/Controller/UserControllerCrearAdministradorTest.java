package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerCrearAdministradorTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;

    @InjectMocks private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private String json(String email, String contrasena) {
        return """
                {"nombre":"Ana","apellidos":"Perez","email":"%s","contrasena":"%s","sede":"Madrid"}
                """.formatted(email, contrasena);
    }

    @Test
    void postAdministradores_datosValidos_devuelve201ConMensajeYSinContrasena() throws Exception {
        when(userService.crearAdministrador(any(AdministradorRegistroDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                        new AdministradorResponseDTO("1", "Ana", "Perez", "ana@esibuy.com",
                                "Madrid", "ADMINISTRADOR", "Administrador creado correctamente")));

        mockMvc.perform(post("/users/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@esibuy.com", "Clave#2026x")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@esibuy.com"))
                .andExpect(jsonPath("$.mensaje").value("Administrador creado correctamente"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void postAdministradores_emailInvalido_devuelve400YNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/users/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("esto-no-es-un-email", "Clave#2026x")))
                .andExpect(status().isBadRequest());

        verify(userService, never()).crearAdministrador(any());
    }

    @Test
    void postAdministradores_contrasenaDebil_devuelve400YNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/users/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@esibuy.com", "debil")))
                .andExpect(status().isBadRequest());

        verify(userService, never()).crearAdministrador(any());
    }
}