
package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceEliminarTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito carrito;

    @BeforeEach
    void setUp() {
        carrito = new Carrito("cliente123");
        carrito.getItems().add(
                new ItemCarrito("producto123", 2)
        );
    }

    @Test
    void eliminarProducto_CarritoValido_EliminaProducto() {

        when(carritoRepository.findByClienteId("cliente123"))
                .thenReturn(Optional.of(carrito));

        carritoService.eliminarProducto("cliente123", "producto123");

        assertTrue(carrito.getItems().isEmpty());

        verify(carritoRepository).save(carrito);
    }

    @Test
    void eliminarProducto_CarritoInexistente_LanzaNotFoundException() {

        when(carritoRepository.findByClienteId("cliente123"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> carritoService.eliminarProducto(
                        "cliente123", "producto123"
                )
        );

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void eliminarProducto_ProductoNoExisteEnCarrito_LanzaNotFoundException() {

        when(carritoRepository.findByClienteId("cliente123"))
                .thenReturn(Optional.of(carrito));

        assertThrows(
                NotFoundException.class,
                () -> carritoService.eliminarProducto(
                        "cliente123", "producto999"
                )
        );

        verify(carritoRepository, never()).save(any());
    }
}
