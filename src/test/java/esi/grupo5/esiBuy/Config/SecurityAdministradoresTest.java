package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.UserController;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityAdministradoresTest {

    private static final String BODY = """
            {"nombre":"Ana","apellidos":"Lopez","email":"ana@esi.es",
             "contrasena":"Abcdef1!x","sede":"Ciudad Real"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void simularLogin(String rol) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("1");
        when(jwtService.extractRol(any())).thenReturn(rol);
    }

    @Test
    void administrador_puedeCrearAdministradores() throws Exception {
        simularLogin("ADMINISTRADOR");
        when(userService.crearAdministrador(any())).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(
                        new AdministradorResponseDTO("1", "Ana", "Lopez", "ana@esi.es",
                                "Ciudad Real", "ADMINISTRADOR", "Administrador creado correctamente")));

        mockMvc.perform(post("/users/administradores")
                .cookie(new Cookie("accessToken", "token"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void cliente_noPuedeCrearAdministradores() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(post("/users/administradores")
                .cookie(new Cookie("accessToken", "token"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinLogin_noPuedeCrearAdministradores() throws Exception {
        mockMvc.perform(post("/users/administradores")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().is4xxClientError());
    }
}