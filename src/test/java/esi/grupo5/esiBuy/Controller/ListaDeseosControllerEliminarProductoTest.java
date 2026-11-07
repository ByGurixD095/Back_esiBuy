package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.ListaDeseosService;

import esi.grupo5.esiBuy.Exception.NotFoundException;

@WebMvcTest(ListaDeseosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListaDeseosControllerEliminarProductoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaDeseosService listaDeseosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void eliminarProductoLista_correctamente_devuelve204() throws Exception {

        mockMvc.perform(delete("/listas-deseos/lista-1/productos/producto-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNoContent());

        verify(listaDeseosService).eliminarProductoLista(
                "lista-1", "producto-1", "cliente-1");
    }

    @Test
    void eliminarProductoLista_listaNoExiste_devuelve404() throws Exception {

        when(listaDeseosService.eliminarProductoLista(
                "lista-1", "producto-1", "cliente-1"))
                .thenThrow(new NotFoundException("Lista de deseos no encontrada"));

        mockMvc.perform(delete("/listas-deseos/lista-1/productos/producto-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNotFound());

        verify(listaDeseosService).eliminarProductoLista(
                "lista-1", "producto-1", "cliente-1");
    }

    @Test
    void eliminarProductoLista_productoNoPertenece_devuelve404() throws Exception {

        when(listaDeseosService.eliminarProductoLista(
                "lista-1", "producto-1", "cliente-1"))
                .thenThrow(new NotFoundException(
                        "El producto no se encuentra en la lista de deseos"));

        mockMvc.perform(delete("/listas-deseos/lista-1/productos/producto-1")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
                .andExpect(status().isNotFound());

        verify(listaDeseosService).eliminarProductoLista(
                "lista-1", "producto-1", "cliente-1");
    }
}