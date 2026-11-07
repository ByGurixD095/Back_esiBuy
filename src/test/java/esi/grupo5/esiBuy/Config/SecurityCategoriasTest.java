package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.CategoriaConsultaController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CategoriaService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(CategoriaConsultaController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityCategoriasTest {

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
    void sinLogin_puedeListarCategoriasPublicas() throws Exception {
        when(categoriaService.listarCategorias()).thenReturn(List.of());

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    void cliente_puedeListarCategoriasPublicas() throws Exception {
        simularLogin("CLIENTE");
        when(categoriaService.listarCategorias()).thenReturn(List.of());

        mockMvc.perform(get("/categorias").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
    }

}
