package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.AdminController;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Service.AdminService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(AdminController.class)
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

    @MockitoBean
    private AdminService adminService;

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
        when(adminService.crearAdministrador(any())).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(
                        AdministradorResponseDTO.builder()
        .id("1").name("Ana").apellidos("Lopez").email("ana@esi.es")
        .sede("Ciudad Real").rol(Rol.ADMINISTRADOR)
        .mensaje("Administrador creado correctamente")
        .build())
        );

        mockMvc.perform(post("/admin")
                .cookie(new Cookie("accessToken", "token"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void cliente_noPuedeCrearAdministradores() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(post("/admin")
                .cookie(new Cookie("accessToken", "token"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinLogin_noPuedeCrearAdministradores() throws Exception {
        mockMvc.perform(post("/admin")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().is4xxClientError());
    }
}