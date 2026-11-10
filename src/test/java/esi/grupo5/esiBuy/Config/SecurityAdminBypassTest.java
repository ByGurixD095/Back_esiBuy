package esi.grupo5.esiBuy.Config;

import esi.grupo5.esiBuy.Controller.AdminController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.AdminService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auditoría defensiva de Broken Access Control (BFLA):
 * Valida que ningún rol no administrador (CLIENTE o VENDEDOR) pueda invocar
 * operaciones exclusivas de administración de usuarios.
 */
@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityAdminBypassTest {

    private static final String ADMIN_REGISTER_BODY = """
            {"nombre":"Infiltrado","apellidos":"Prueba","email":"infiltrado@esi.es",
             "contrasena":"Password123!","confirmarContrasena":"Password123!",
             "sede":"Ciudad Real"}
            """;

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private UserService userService;
    @MockitoBean private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void simularUsuarioAutenticado(String id, String role) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn(id);
        when(jwtService.extractRol(any())).thenReturn(role);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse(id)).thenReturn(true);
    }

    @Test
    @DisplayName("VENDEDOR no debe poder registrar nuevos administradores en POST /admin")
    void vendedorNoPuedeCrearAdministradores() throws Exception {
        simularUsuarioAutenticado("vendedor-1", "VENDEDOR");

        mockMvc.perform(post("/admin")
                        .cookie(new Cookie("accessToken", "token_vendedor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADMIN_REGISTER_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENTE no debe poder bloquear usuarios en PATCH /admin/bloquear/{id}")
    void clienteNoPuedeBloquearUsuarios() throws Exception {
        simularUsuarioAutenticado("cliente-1", "CLIENTE");

        mockMvc.perform(patch("/admin/bloquear/victima-1")
                        .cookie(new Cookie("accessToken", "token_cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VENDEDOR no debe poder desbloquear usuarios en PATCH /admin/desbloquear/{id}")
    void vendedorNoPuedeDesbloquearUsuarios() throws Exception {
        simularUsuarioAutenticado("vendedor-1", "VENDEDOR");

        mockMvc.perform(patch("/admin/desbloquear/victima-1")
                        .cookie(new Cookie("accessToken", "token_vendedor")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENTE no debe poder activar usuarios en PATCH /admin/activar/{id}")
    void clienteNoPuedeActivarUsuarios() throws Exception {
        simularUsuarioAutenticado("cliente-1", "CLIENTE");

        mockMvc.perform(patch("/admin/activar/victima-1")
                        .cookie(new Cookie("accessToken", "token_cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLIENTE no debe poder eliminar usuarios en DELETE /admin/{id}")
    void clienteNoPuedeEliminarUsuarios() throws Exception {
        simularUsuarioAutenticado("cliente-1", "CLIENTE");

        mockMvc.perform(delete("/admin/victima-1")
                        .cookie(new Cookie("accessToken", "token_cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Petición sin autenticación debe ser rechazada con 401/403")
    void sinAutenticacionDebeSerRechazada() throws Exception {
        mockMvc.perform(delete("/admin/victima-1"))
                .andExpect(status().is4xxClientError());
    }
}
