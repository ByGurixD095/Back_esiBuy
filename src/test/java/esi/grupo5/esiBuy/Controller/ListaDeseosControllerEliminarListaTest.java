
package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Service.ListaDeseosService;
import esi.grupo5.esiBuy.Service.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListaDeseosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListaDeseosControllerEliminarListaTest {

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
    void eliminarListaCorrectamente() throws Exception {
        mockMvc.perform(delete("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNoContent());

        verify(listaDeseosService).eliminarLista("lista-1", "cliente-1");
    }

    @Test
    void eliminarListaInexistente() throws Exception {
        doThrow(new NotFoundException("Lista de deseos no encontrada"))
                .when(listaDeseosService).eliminarLista("lista-1", "cliente-1");

        mockMvc.perform(delete("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarListaDeOtroCliente() throws Exception {
        doThrow(new NotFoundException("Lista de deseos no encontrada"))
                .when(listaDeseosService).eliminarLista("lista-1", "cliente-2");

        mockMvc.perform(delete("/listas-deseos/lista-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-2", null)))
                .andExpect(status().isNotFound());
    }
}
