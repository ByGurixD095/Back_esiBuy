package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import esi.grupo5.esiBuy.Dto.CrearPedidoRequestDTO;
import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.EstadoPedido;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.Pedido;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crearPedido_guardaSnapshotDescuentaStockYVacíaCarrito() {
        Carrito carrito = carritoConProducto();
        Producto producto = producto();
        MetodoPago metodoPago = metodoPago();
        prepararCheckout(carrito, producto, metodoPago);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId("pedido-1");
            return pedido;
        });
        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Carrito.class)))
                .thenReturn(new Carrito("cliente-1"));

        var resultado = pedidoService.crearPedido("cliente-1", solicitud());

        assertEquals("pedido-1", resultado.id());
        assertEquals(2_500L, resultado.totalCent());
        assertEquals(EstadoPedido.RECIBIDO, resultado.estado());
        assertEquals(1, resultado.lineas().size());
        assertEquals(2, resultado.lineas().get(0).cantidad());
        assertEquals(1_250, resultado.lineas().get(0).precioUnitarioCent());
        assertEquals("**** 1234", resultado.referenciaMetodoPago());
        assertEquals("Calle Mayor 1", resultado.direccionEnvio().direccion());
        verify(mongoTemplate).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Carrito.class));
    }

    @Test
    void crearPedido_rechazaCarritoVacio() {
        when(carritoRepository.findByClienteId("cliente-1"))
                .thenReturn(Optional.of(new Carrito("cliente-1")));

        assertThrows(ValidationException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void crearPedido_rechazaMetodoDePagoDeOtroCliente() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carritoConProducto()));
        when(metodoPagoRepository.findByIdAndClienteId("pago-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(mongoTemplate, never()).updateFirst(
                any(Query.class), any(Update.class), eq(Producto.class));
    }

    @Test
    void crearPedido_siNoQuedaStockNoGuardaPedidoNiVacíaCarrito() {
        Carrito carrito = carritoConProducto();
        prepararCheckout(carrito, producto(), metodoPago());
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(UpdateResult.acknowledged(0L, 0L, null));

        assertThrows(ConflictException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(mongoTemplate, never()).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Carrito.class));
    }

    @Test
    void crearPedido_siFallaUnaReservaRestauraElStockReservadoPreviamente() {
        Carrito carrito = carritoConProducto();
        carrito.getItems().add(new ItemCarrito("producto-2", 1));
        prepararCheckout(carrito, producto(), metodoPago());
        Producto segundoProducto = producto();
        segundoProducto.setId("producto-2");
        segundoProducto.setNombre("Ratón");
        when(productoRepository.findById("producto-2")).thenReturn(Optional.of(segundoProducto));
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(
                        UpdateResult.acknowledged(1L, 1L, null),
                        UpdateResult.acknowledged(0L, 0L, null),
                        UpdateResult.acknowledged(1L, 1L, null));

        assertThrows(ConflictException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(mongoTemplate, org.mockito.Mockito.times(3)).updateFirst(
                any(Query.class), any(Update.class), eq(Producto.class));
    }

    @Test
    void crearPedido_siElCarritoCambiaReviertePedidoYStock() {
        Carrito carrito = carritoConProducto();
        prepararCheckout(carrito, producto(), metodoPago());
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(
                        UpdateResult.acknowledged(1L, 1L, null),
                        UpdateResult.acknowledged(1L, 1L, null));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId("pedido-1");
            return pedido;
        });
        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Carrito.class)))
                .thenReturn(null);

        assertThrows(ConflictException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(pedidoRepository).deleteById("pedido-1");
        verify(mongoTemplate, org.mockito.Mockito.times(2)).updateFirst(
                any(Query.class), any(Update.class), eq(Producto.class));
    }

    @Test
    void obtenerPedido_noPermiteConsultarPedidosDeOtroCliente() {
        when(pedidoRepository.findByIdAndClienteId("pedido-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> pedidoService.obtenerPedido("cliente-1", "pedido-1"));
    }

    private void prepararCheckout(Carrito carrito, Producto producto, MetodoPago metodoPago) {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(metodoPagoRepository.findByIdAndClienteId("pago-1", "cliente-1"))
                .thenReturn(Optional.of(metodoPago));
        when(productoRepository.findById("producto-1")).thenReturn(Optional.of(producto));
    }

    private Carrito carritoConProducto() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.setId("carrito-1");
        carrito.setItems(new ArrayList<>(List.of(new ItemCarrito("producto-1", 2))));
        return carrito;
    }

    private Producto producto() {
        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setNombre("Teclado");
        producto.setReferencia("TEC-1");
        producto.setPrecioCent(1_250);
        producto.setNumStock(4);
        producto.setActivo(true);
        return producto;
    }

    private MetodoPago metodoPago() {
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId("pago-1");
        metodoPago.setTipo(TipoMetodoPago.TARJETA);
        metodoPago.setAlias("Personal");
        metodoPago.setReferencia("**** 1234");
        return metodoPago;
    }

    private CrearPedidoRequestDTO solicitud() {
        return new CrearPedidoRequestDTO(
                "pago-1",
                new DireccionEnvioDTO(
                        "Ana García",
                        "Calle Mayor 1",
                        "Madrid",
                        "Madrid",
                        "28001",
                        "España",
                        "+34600000000"));
    }
}
