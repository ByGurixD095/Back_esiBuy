package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import esi.grupo5.esiBuy.Controller.FavoritoController;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.FavoritoService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(FavoritoController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityFavoritosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private FavoritoService favoritoService;

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
    void cliente_puedeListarMarcarYQuitarFavoritos() throws Exception {
        simularLogin("CLIENTE");
        when(favoritoService.listarFavoritos("1")).thenReturn(List.of());

        mockMvc.perform(get("/favoritos").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/favoritos/p1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/favoritos/p1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void vendedor_noPuedeUsarFavoritos() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(get("/favoritos").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/favoritos/p1").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administrador_noPuedeUsarFavoritos() throws Exception {
        simularLogin("ADMINISTRADOR");

        mockMvc.perform(get("/favoritos").cookie(new Cookie("accessToken", "token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinLogin_noPuedeUsarFavoritos() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().is4xxClientError());
    }
}