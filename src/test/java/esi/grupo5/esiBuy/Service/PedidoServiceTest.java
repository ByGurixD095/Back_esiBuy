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
import java.time.LocalDateTime;

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
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.DireccionEnvioPedido;
import esi.grupo5.esiBuy.Model.LineaPedido;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.Pedido;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Model.enums.EstadoPedido;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

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
    private UsuarioRepository usuarioRepository;

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
        assertEquals(EstadoPedido.EN_PROCESO, resultado.estado());
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
    void crearPedido_aplicaElDescuentoPremiumAlPrecioUnitarioYTotal() {
        Carrito carrito = carritoConProducto();
        Producto producto = producto();
        producto.setDescuento(20);
        producto.setDescuentoPremium(40);
        prepararCheckout(carrito, producto, metodoPago());
        Cliente cliente = new Cliente();
        cliente.setTipoCliente(TipoCliente.PREMIUM);
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId("pedido-premium");
            return pedido;
        });
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(Carrito.class))).thenReturn(carrito);

        var resultado = pedidoService.crearPedido("cliente-1", solicitud());

        assertEquals(750, resultado.lineas().get(0).precioUnitarioCent());
        assertEquals(1_500, resultado.totalCent());
    }

    @Test
    void crearPedido_rechazaProductoExclusivoParaClienteNormal() {
        Carrito carrito = carritoConProducto();
        Producto producto = producto();
        producto.setSoloPremium(true);
        prepararCheckout(carrito, producto, metodoPago());

        assertThrows(ValidationException.class,
                () -> pedidoService.crearPedido("cliente-1", solicitud()));
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(Producto.class));
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

    @Test
    void pagarPedido_cambiaAutomaticamenteAEstadoVisibleParaVendedor() {
        Pedido pedido = pedido("pedido-1", EstadoPedido.EN_PROCESO);
        pedido.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));
        when(pedidoRepository.findByIdAndClienteId("pedido-1", "cliente-1"))
                .thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = pedidoService.pagarPedido("cliente-1", "pedido-1");

        assertEquals(EstadoPedido.PAGADO, resultado.estado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void marcarEnviado_soloActualizaLineasDelVendedorYFinalizaCuandoTodasSeEnvian() {
        Pedido pedido = pedido("pedido-1", EstadoPedido.PAGADO);
        pedido.getLineas().add(new LineaPedido(
                "producto-2", "Ratón", "RAT-2", null, "vendedor-2", 500, 1, 500));
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var respuestaVendedorUno = pedidoService.marcarEnviado("vendedor-1", "pedido-1");

        assertEquals(EstadoPedido.PAGADO, respuestaVendedorUno.estado());
        assertEquals(1, respuestaVendedorUno.lineas().size());
        assertEquals(1_250, respuestaVendedorUno.totalCent());
        assertEquals(null, respuestaVendedorUno.referenciaMetodoPago());
        assertEquals(true, respuestaVendedorUno.lineas().get(0).enviado());
        assertEquals(false, pedido.getLineas().get(1).isEnviado());

        pedidoService.marcarEnviado("vendedor-2", "pedido-1");

        assertEquals(EstadoPedido.TRAMITADO_ENVIADO, pedido.getEstado());
    }

    @Test
    void confirmarEntrega_finalizaPedidoYLoPasaAlHistorico() {
        Pedido pedido = pedido("pedido-1", EstadoPedido.TRAMITADO_ENVIADO);
        pedido.getLineas().get(0).setEnviado(true);
        when(pedidoRepository.findByIdAndClienteId("pedido-1", "cliente-1"))
                .thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = pedidoService.confirmarEntrega("cliente-1", "pedido-1");

        assertEquals(EstadoPedido.FINALIZADO, respuesta.estado());
    }

    @Test
    void cancelarPedido_restauraStockYNoPermiteCancelarDespuesDelEnvio() {
        Pedido pedido = pedido("pedido-1", EstadoPedido.PAGADO);
        when(pedidoRepository.findByIdAndClienteId("pedido-1", "cliente-1"))
                .thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

        var respuesta = pedidoService.cancelarPedido("cliente-1", "pedido-1");

        assertEquals(EstadoPedido.CANCELADO, respuesta.estado());
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Producto.class));

        Pedido enviado = pedido("pedido-2", EstadoPedido.PAGADO);
        enviado.getLineas().get(0).setEnviado(true);
        when(pedidoRepository.findByIdAndClienteId("pedido-2", "cliente-1"))
                .thenReturn(Optional.of(enviado));
        assertThrows(ConflictException.class,
                () -> pedidoService.cancelarPedido("cliente-1", "pedido-2"));
    }

    @Test
    void expirarPedidosPendientesDePago_cancelaYDevuelveStock() {
        Pedido pedido = pedido("pedido-1", EstadoPedido.EN_PROCESO);
        pedido.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));
        when(pedidoRepository.findByEstadoAndFechaExpiracionLessThanEqual(
                eq(EstadoPedido.EN_PROCESO), any(LocalDateTime.class))).thenReturn(List.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Producto.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

        pedidoService.expirarPedidosPendientesDePago();

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Producto.class));
    }

    @Test
    void listarPedidosVendedor_migraPedidosAntiguosYDevuelveSoloSusLineas() {
        Pedido antiguo = pedido("pedido-antiguo", EstadoPedido.RECIBIDO);
        antiguo.getLineas().get(0).setIdVendedor(null);
        when(pedidoRepository.findByEstado(EstadoPedido.RECIBIDO)).thenReturn(List.of(antiguo));
        when(productoRepository.findById("producto-1")).thenReturn(Optional.of(producto()));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.findByLineas_IdVendedorAndEstadoIn(
                eq("vendedor-1"), any())).thenReturn(List.of(antiguo));

        var resultado = pedidoService.listarPedidosVendedor("vendedor-1", false);

        assertEquals(1, resultado.size());
        assertEquals(EstadoPedido.PAGADO, resultado.get(0).estado());
        assertEquals(1, resultado.get(0).lineas().size());
    }

    private Pedido pedido(String id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setClienteId("cliente-1");
        pedido.setEstado(estado);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setNombreCliente("Ana García");
        pedido.setDireccionEnvio(new DireccionEnvioPedido(
                "Ana García", "Calle Mayor 1", "Madrid", "Madrid", "28001", "España", "+34600000000"));
        pedido.setLineas(new ArrayList<>(List.of(new LineaPedido(
                "producto-1", "Teclado", "TEC-1", null, "vendedor-1", 1_250, 1, 1_250))));
        return pedido;
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
        producto.setIdVendedor("vendedor-1");
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
