package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import esi.grupo5.esiBuy.Controller.CategoriaController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CategoriaService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(CategoriaController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityCategoriasEliminarTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void simularLogin(String rol) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("1");
        when(jwtService.extractRol(any())).thenReturn(rol);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse("1")).thenReturn(true);
    }

    @Test
    void administrador_puedeEliminarCategorias() throws Exception {
        simularLogin("ADMINISTRADOR");

        mockMvc.perform(delete("/admin/categorias/c1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void vendedor_noPuedeEliminarCategorias() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(delete("/admin/categorias/c1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void cliente_noPuedeEliminarCategorias() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(delete("/admin/categorias/c1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinLogin_noPuedeEliminarCategorias() throws Exception {
        mockMvc.perform(delete("/admin/categorias/c1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void administrador_puedeModificarCategorias() throws Exception {
        simularLogin("ADMINISTRADOR");

        mockMvc.perform(put("/admin/categorias/c1")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Hogar\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_noPuedeModificarCategorias() throws Exception {
        simularLogin("CLIENTE");

        mockMvc.perform(put("/admin/categorias/c1")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Hogar\"}"))
                .andExpect(status().isForbidden());
    }
}