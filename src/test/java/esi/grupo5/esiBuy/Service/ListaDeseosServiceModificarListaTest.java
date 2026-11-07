
package esi.grupo5.esiBuy.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaDeseosServiceModificarListaTest {

    @Mock
    private ListaDeseosRepository listaDeseosRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ListaDeseosService listaDeseosService;

    private ListaDeseos lista;

    @BeforeEach
    void setUp() {
        lista = new ListaDeseos();
        lista.setId("lista-1");
        lista.setIdCliente("cliente-1");
        lista.setNombre("Importante");
        lista.setProductosIds(new HashSet<>(Set.of("producto-1", "producto-2")));
    }

    @Test
    void modificarNombreListaCorrectamente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1"))
                .thenReturn(Optional.of(lista));
        when(listaDeseosRepository.save(lista)).thenReturn(lista);

        ListaDeseos resultado = listaDeseosService.modificarLista(
                "lista-1", "Favoritos", "cliente-1");

        assertEquals("Favoritos", resultado.getNombre());
        assertEquals(2, resultado.getProductosIds().size());
        assertTrue(resultado.getProductosIds().contains("producto-1"));
        assertTrue(resultado.getProductosIds().contains("producto-2"));

        verify(listaDeseosRepository).save(lista);
    }

    @Test
    void modificarListaInexistente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.modificarLista(
                        "lista-1", "Favoritos", "cliente-1"));

        verify(listaDeseosRepository, never()).save(any(ListaDeseos.class));
    }

    @Test
    void modificarListaDeOtroCliente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-2"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.modificarLista(
                        "lista-1", "Favoritos", "cliente-2"));

        verify(listaDeseosRepository, never()).save(any(ListaDeseos.class));
    }
}
