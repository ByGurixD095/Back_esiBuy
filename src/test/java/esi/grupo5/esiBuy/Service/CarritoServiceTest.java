package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void listarCarrito_carritoVacioDevuelveListaVacia() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());

        assertTrue(carritoService.listarCarrito("cliente-1").isEmpty());

    }

    @Test
    void listarCarrito_conProductosDevuelveDatosYCantidad() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.setItems(List.of(new ItemCarrito("producto-1", 3)));

        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setNombre("Ratón inalámbrico");
        producto.setPrecioCent(1999);
        producto.setUrlImagen("raton.jpg");

        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(productoRepository.findById("producto-1")).thenReturn(Optional.of(producto));

        List<ItemCarritoResponseDTO> resultado = carritoService.listarCarrito("cliente-1");
        assertEquals(1, resultado.size());
        assertEquals("producto-1", resultado.get(0).productoId());
        assertEquals("Ratón inalámbrico", resultado.get(0).nombre());
        assertEquals(1999, resultado.get(0).precioCent());
        assertEquals("raton.jpg", resultado.get(0).urlImagen());
        assertEquals(3, resultado.get(0).cantidad());
    }

    @Test
    void listarCarrito_conProductosConsultaLosDatosDelProducto() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.setItems(List.of(new ItemCarrito("producto-1", 3)));

        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setNombre("Ratón inalámbrico");
        producto.setPrecioCent(1999);

        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(productoRepository.findById("producto-1")).thenReturn(Optional.of(producto));

        carritoService.listarCarrito("cliente-1");

        verify(productoRepository).findById("producto-1");
    }

}
