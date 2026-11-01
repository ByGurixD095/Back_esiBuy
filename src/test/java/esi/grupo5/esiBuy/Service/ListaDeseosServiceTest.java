package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith (MockitoExtension.class)
public class ListaDeseosServiceTest {
    
    @Mock
    private ListaDeseosRepository listaDeseosRepository;

    @Mock
    private ProductoRepository productoRepository;

    private ListaDeseosService service;

    @BeforeEach
    void setUp() {
        service = new ListaDeseosService(listaDeseosRepository, productoRepository);
    }

    @Test
    void obtenerListas_devuelveLasListasDelCliente() {
       ListaDeseos lista = new ListaDeseos();
       lista.setId("lista-1");
       lista.setIdCliente("cliente-1");
       lista.setNombre("Para comprar");

        when(listaDeseosRepository.findByIdCliente("cliente-1")).thenReturn(List.of(lista));

        List<ListaDeseos> resultado = service.obtenerListas("cliente-1");

        assertEquals(1, resultado.size());
        assertEquals("Para comprar", resultado.get(0).getNombre());
        assertEquals("cliente-1", resultado.get(0).getIdCliente());

        verify(listaDeseosRepository).findByIdCliente("cliente-1"); 
    }

    @Test
    void obtenerProductosLista_devuelveLosProductos() {
        ListaDeseos lista = new ListaDeseos();
        lista.setId("lista-1");
        lista.setIdCliente("cliente-1");
        lista.setProductosIds(Set.of("producto-1"));

        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setNombre("Teclado");

        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1")).thenReturn(Optional.of(lista));
        when(productoRepository.findAllById(lista.getProductosIds())).thenReturn(List.of(producto));

        List<Producto> resultado = service.obtenerProductosLista("lista-1", "cliente-1");

        assertEquals(1, resultado.size());
        assertEquals("producto-1", resultado.get(0).getId());
        assertEquals("Teclado", resultado.get(0).getNombre());

        verify(listaDeseosRepository).findByIdAndIdCliente("lista-1", "cliente-1");
        verify(productoRepository).findAllById(lista.getProductosIds());
    }

    @Test
    void obtenerProductosLista_listaNoEncontrada_lanzaExcepcion() {
        when(listaDeseosRepository.findByIdAndIdCliente("lista-1", "cliente-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.obtenerProductosLista("lista-1", "cliente-1"));

        verify(listaDeseosRepository).findByIdAndIdCliente("lista-1", "cliente-1");
        verifyNoInteractions(productoRepository);
    }
    

}
