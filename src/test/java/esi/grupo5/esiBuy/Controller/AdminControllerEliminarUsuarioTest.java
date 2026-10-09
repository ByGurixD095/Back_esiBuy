package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Config.JwtAuthenticationFilter;
import esi.grupo5.esiBuy.Config.SecurityConfig;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminControllerEliminarUsuarioTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void limpiarContextoAntesDeCadaTest() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContextoDespuesDeCadaTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void eliminarUsuario_administradorAutenticado_devuelve204YDelegaAlServicio() throws Exception {
        simularSesion("ADMINISTRADOR", "admin-1");

        mockMvc.perform(delete("/admin/cliente-1")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());

        verify(adminService).eliminarUsuario("cliente-1", "admin-1");
    }

    @Test
    void eliminarUsuario_usuarioSinRolAdministrador_devuelve403YSinLlamarAlServicio() throws Exception {
        simularSesion("CLIENTE", "cliente-2");

        mockMvc.perform(delete("/admin/cliente-1")
                        .cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());

        verify(adminService, never()).eliminarUsuario(any(), any());
    }

    @Test
    void eliminarUsuario_sinSesion_rechazaLaPeticion() throws Exception {
        mockMvc.perform(delete("/admin/cliente-1"))
                .andExpect(status().is4xxClientError());

        verify(adminService, never()).eliminarUsuario(any(), any());
    }

    private void simularSesion(String rol, String id) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn(id);
        when(jwtService.extractRol(any())).thenReturn(rol);
    }
}
