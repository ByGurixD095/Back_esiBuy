package esi.grupo5.esiBuy.Config;

import esi.grupo5.esiBuy.Controller.AdminController;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Service.AdminService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityAdminAuthorizationTest {

    private static final String BODY = """
            {"nombre":"Ana","apellidos":"Lopez","email":"ana@esi.es",
             "contrasena":"Abcdef1!x","sede":"Ciudad Real"}
            """;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private UserService userService;

    @BeforeEach
    void clearSecurityContextBeforeTest() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContextAfterTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void administradorPuedeCrearAdministradores() throws Exception {
        stubAuthenticatedRole("ADMINISTRADOR");
        when(adminService.crearAdministrador(any())).thenReturn(ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AdministradorResponseDTO.builder()
                        .id("1")
                        .name("Ana")
                        .apellidos("Lopez")
                        .email("ana@esi.es")
                        .sede("Ciudad Real")
                        .rol(esi.grupo5.esiBuy.Model.enums.Rol.ADMINISTRADOR)
                        .mensaje("Administrador creado correctamente")
                        .build()));

        mockMvc.perform(post("/admin")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void clienteNoPuedeCrearAdministradores() throws Exception {
        stubAuthenticatedRole("CLIENTE");

        mockMvc.perform(post("/admin")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinSesionNoPuedeCrearAdministradores() throws Exception {
        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void administradorPuedeConsultarUsuarios() throws Exception {
        stubAuthenticatedRole("ADMINISTRADOR");
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeConsultarUsuarios() throws Exception {
        stubAuthenticatedRole("CLIENTE");

        mockMvc.perform(get("/admin/users")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
    }

    private void stubAuthenticatedRole(String role) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("user-1");
        when(jwtService.extractRol(any())).thenReturn(role);
    }

    @Test
    void ClienteNoPuedeModificarUsuarios() throws Exception {
        stubAuthenticatedRole("CLIENTE");

        mockMvc.perform(patch("/admin/user-1")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"NuevoNombre"}
                                """))
                .andExpect(status().isForbidden());
    }


}
