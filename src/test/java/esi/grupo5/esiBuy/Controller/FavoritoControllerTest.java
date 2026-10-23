package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.FavoritoService;
import esi.grupo5.esiBuy.Service.JwtService;

@WebMvcTest(FavoritoController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoritoService favoritoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void marcarFavorito_devuelve204YUsaElIdDelToken() throws Exception {
        mockMvc.perform(post("/favoritos/p1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNoContent());

        verify(favoritoService).marcarFavorito("cliente-1", "p1");
    }

    @Test
    void marcarFavorito_productoInexistenteDevuelve404ConMensajeNeutro() throws Exception {
        doThrow(new NotFoundException("Producto no encontrado"))
                .when(favoritoService).marcarFavorito("cliente-1", "nope");

        mockMvc.perform(post("/favoritos/nope")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }

    @Test
    void listarFavoritos_devuelve200ConLosProductosDelCliente() throws Exception {
        Producto producto = new Producto();
        producto.setId("p1");
        producto.setNombre("Camiseta ESI");
        when(favoritoService.listarFavoritos("cliente-1")).thenReturn(List.of(producto));

        mockMvc.perform(get("/favoritos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("p1"))
                .andExpect(jsonPath("$[0].nombre").value("Camiseta ESI"));
    }

    @Test
    void quitarFavorito_devuelve204YUsaElIdDelToken() throws Exception {
        mockMvc.perform(delete("/favoritos/p1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNoContent());

        verify(favoritoService).quitarFavorito("cliente-1", "p1");
    }
}