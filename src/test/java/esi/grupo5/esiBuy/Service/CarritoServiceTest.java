package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.ItemCarrito;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void listarCarrito_carritoVacioDevuelveListaVacia() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());

        assertTrue(carritoService.listarCarrito("cliente-1").isEmpty());

    }

    @Test
    void listarCarrito_conProductosDevuelveSusProductosYCantidades() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.setItems(List.of(new ItemCarrito("producto-1", 3)));

        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));

        List<ItemCarrito> resultado = carritoService.listarCarrito("cliente-1");
        assertEquals(1, resultado.size());
        assertEquals("producto-1", resultado.get(0).getProductoId());
        assertEquals(3, resultado.get(0).getCantidad());
    }



}
