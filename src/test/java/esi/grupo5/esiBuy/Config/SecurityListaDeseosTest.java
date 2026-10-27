package esi.grupo5.esiBuy.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.ListaDeseosController;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.ListaDeseosService;
import jakarta.servlet.http.Cookie;

@WebMvcTest(ListaDeseosController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityListaDeseosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ListaDeseosService listaDeseosService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void simularLogin(String rol) {
        when(jwtService.isTokenValid(any())).thenReturn(true);
        when(jwtService.extractId(any())).thenReturn("cliente-1");
        when(jwtService.extractRol(any())).thenReturn(rol);
        when(usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse("cliente-1"))
                .thenReturn(true);
    }

    @Test
    void cliente_puedeCrearListaDeDeseos() throws Exception {
        simularLogin("CLIENTE");
        when(listaDeseosService.crearLista("Compras", "cliente-1"))
                .thenReturn(new ListaDeseos());

        mockMvc.perform(post("/listas-deseos")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Compras"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void vendedor_noPuedeCrearListaDeDeseos() throws Exception {
        simularLogin("VENDEDOR");

        mockMvc.perform(post("/listas-deseos")
                        .cookie(new Cookie("accessToken", "token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Compras"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listaDeseosService);
    }

    @Test
    void sinAutenticacion_noPuedeCrearListaDeDeseos() throws Exception {
        mockMvc.perform(post("/listas-deseos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Compras"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(listaDeseosService);
    }
}
