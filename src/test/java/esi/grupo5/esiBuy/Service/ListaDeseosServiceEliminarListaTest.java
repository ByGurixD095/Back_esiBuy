
package esi.grupo5.esiBuy.Service;

import java.util.Optional;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaDeseosServiceEliminarListaTest {

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
    }

    @Test
    void eliminarListaCorrectamente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1"))
                .thenReturn(Optional.of(lista));

        assertDoesNotThrow(() ->
                listaDeseosService.eliminarLista("lista-1", "cliente-1"));

        verify(listaDeseosRepository).delete(lista);
    }

    @Test
    void eliminarListaInexistente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.eliminarLista("lista-1", "cliente-1"));

        verify(listaDeseosRepository, never()).delete(any(ListaDeseos.class));
    }

    @Test
    void eliminarListaDeOtroCliente() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-2"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.eliminarLista("lista-1", "cliente-2"));

        verify(listaDeseosRepository, never()).delete(any(ListaDeseos.class));
    }
}
