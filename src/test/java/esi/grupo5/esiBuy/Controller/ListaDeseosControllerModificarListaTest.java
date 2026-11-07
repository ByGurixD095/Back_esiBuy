
package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Service.ListaDeseosService;
import esi.grupo5.esiBuy.Service.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ListaDeseosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListaDeseosControllerModificarListaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaDeseosService listaDeseosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ListaDeseosRepository listaDeseosRepository;

    @Test
    void modificarListaCorrectamente() throws Exception {
        ListaDeseos lista = new ListaDeseos();
        lista.setId("lista-1");
        lista.setIdCliente("cliente-1");
        lista.setNombre("Favoritos");

        when(listaDeseosService.modificarLista("lista-1", "Favoritos", "cliente-1"))
                .thenReturn(lista);

        mockMvc.perform(put("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Favoritos"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Favoritos"));

        verify(listaDeseosService).modificarLista("lista-1", "Favoritos", "cliente-1");
    }

    @Test
    void modificarListaInexistente() throws Exception {
        when(listaDeseosService.modificarLista("lista-1", "Favoritos", "cliente-1"))
                .thenThrow(new NotFoundException("Lista de deseos no encontrada"));

        mockMvc.perform(put("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Favoritos"
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void modificarListaConNombreVacio() throws Exception {
        mockMvc.perform(put("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "   "
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(listaDeseosService, never())
                .modificarLista(anyString(), anyString(), anyString());
    }
}
