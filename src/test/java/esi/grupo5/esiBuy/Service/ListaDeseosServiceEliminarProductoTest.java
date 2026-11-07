package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.ListaDeseosRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ListaDeseosServiceEliminarProductoTest {

    @Mock
    private ListaDeseosRepository listaDeseosRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ListaDeseosService listaDeseosService;

    private ListaDeseos lista;

    private final String idLista = "lista-1";
    private final String idCliente = "cliente-1";
    private final String idProducto = "producto-1";

    @BeforeEach
    void setUp() {
        lista = new ListaDeseos();
        lista.setId(idLista);
        lista.setIdCliente(idCliente);
        lista.setNombre("Mis favoritos");

        Set<String> productos = new HashSet<>();
        productos.add(idProducto);
        productos.add("producto-2");

        lista.setProductosIds(productos);
    }

    @Test
    void eliminarProductoLista_correctamente() {

        when(listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente))
                .thenReturn(Optional.of(lista));

        when(listaDeseosRepository.save(any(ListaDeseos.class)))
                .thenReturn(lista);

        ListaDeseos resultado = listaDeseosService.eliminarProductoLista(
                idLista, idProducto, idCliente);

        assertEquals(1, resultado.getProductosIds().size());
        assertEquals(false, resultado.getProductosIds().contains(idProducto));
        assertEquals(true, resultado.getProductosIds().contains("producto-2"));

        verify(listaDeseosRepository).save(lista);
    }

    @Test
    void eliminarProductoLista_listaNoExiste() {

        when(listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.eliminarProductoLista(
                        idLista, idProducto, idCliente));

        verify(listaDeseosRepository, never()).save(any());
    }

    @Test
    void eliminarProductoLista_productoNoPerteneceALista() {

        when(listaDeseosRepository.findByIdAndIdCliente(idLista, idCliente))
                .thenReturn(Optional.of(lista));

        assertThrows(NotFoundException.class, () ->
                listaDeseosService.eliminarProductoLista(
                        idLista, "producto-inexistente", idCliente));

        verify(listaDeseosRepository, never()).save(any());
    }
}