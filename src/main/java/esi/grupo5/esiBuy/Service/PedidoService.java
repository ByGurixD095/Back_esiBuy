package esi.grupo5.esiBuy.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import esi.grupo5.esiBuy.Dto.CrearPedidoRequestDTO;
import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Dto.LineaPedidoResponseDTO;
import esi.grupo5.esiBuy.Dto.PedidoResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.DireccionEnvioPedido;
import esi.grupo5.esiBuy.Model.EstadoPedido;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.LineaPedido;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.Pedido;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

/** Gestiona el checkout, los estados de pedido, el stock y las vistas de cliente y vendedor. */
@Service
public class PedidoService {

    private static final int PLAZO_PAGO_MINUTOS = 30;
    private static final Set<EstadoPedido> ESTADOS_VISIBLES_VENDEDOR =
            Set.of(EstadoPedido.PAGADO, EstadoPedido.TRAMITADO_ENVIADO);

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MongoTemplate mongoTemplate;

    public PedidoService(
            PedidoRepository pedidoRepository,
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            MongoTemplate mongoTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public PedidoResponseDTO crearPedido(String clienteId, CrearPedidoRequestDTO dto) {
        if (dto == null || dto.direccionEnvio() == null
                || dto.metodoPagoId() == null || dto.metodoPagoId().isBlank()) {
            throw new ValidationException("El método de pago y la dirección de envío son obligatorios");
        }

        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new NotFoundException("Carrito no encontrado"));
        List<ItemCarrito> items = carrito.getItems();
        if (items == null || items.isEmpty()) {
            throw new ValidationException("No se puede realizar un pedido con el carrito vacío");
        }

        MetodoPago metodoPago = metodoPagoRepository.findByIdAndClienteId(dto.metodoPagoId(), clienteId)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));

        Pedido pedido = construirPedido(clienteId, items, dto.direccionEnvio(), metodoPago);
        List<LineaPedido> stockReservado = new ArrayList<>();
        Pedido pedidoGuardado = null;

        try {
            for (LineaPedido linea : pedido.getLineas()) {
                Query productoDisponible = Query.query(Criteria.where("id").is(linea.getProductoId())
                        .and("activo").is(true)
                        .and("numStock").gte(linea.getCantidad()));
                UpdateResult resultado = mongoTemplate.updateFirst(
                        productoDisponible,
                        new Update().inc("numStock", -linea.getCantidad()),
                        Producto.class);

                if (resultado.getMatchedCount() != 1) {
                    throw new ConflictException(
                            "El producto " + linea.getNombre() + " ya no tiene stock suficiente");
                }
                stockReservado.add(linea);
            }

            pedidoGuardado = pedidoRepository.save(pedido);
            PedidoResponseDTO respuesta = convertirAResponseDTO(pedidoGuardado);
            Query carritoSinCambios = Query.query(Criteria.where("id").is(carrito.getId())
                    .and("clienteId").is(clienteId)
                    .and("items").is(items));
            Carrito carritoVaciado = mongoTemplate.findAndModify(
                    carritoSinCambios,
                    new Update().set("items", List.of()),
                    FindAndModifyOptions.options().returnNew(true),
                    Carrito.class);

            if (carritoVaciado == null) {
                throw new ConflictException("El carrito ha cambiado; vuelve a revisar y confirmar el pedido");
            }

            return respuesta;
        } catch (RuntimeException error) {
            revertirPedido(pedidoGuardado, stockReservado, error);
            throw error;
        }
    }

    public List<PedidoResponseDTO> listarPedidos(String clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PedidoResponseDTO> listarPedidosCliente(String clienteId, boolean historico) {
        Set<EstadoPedido> estados = historico
                ? Set.of(EstadoPedido.FINALIZADO, EstadoPedido.CANCELADO)
                : Set.of(EstadoPedido.EN_PROCESO, EstadoPedido.PAGADO,
                        EstadoPedido.TRAMITADO_ENVIADO, EstadoPedido.RECIBIDO);
        return pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId).stream()
                .filter(pedido -> estados.contains(pedido.getEstado()))
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public PedidoResponseDTO obtenerPedido(String clienteId, String pedidoId) {
        Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, clienteId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        return convertirAResponseDTO(pedido);
    }

    private Pedido construirPedido(
            String clienteId,
            List<ItemCarrito> items,
            DireccionEnvioDTO direccionDTO,
            MetodoPago metodoPago) {
        List<LineaPedido> lineas = new ArrayList<>();
        long totalCent = 0;

        try {
            for (ItemCarrito item : items) {
                if (item == null || item.getCantidad() < 1 || item.getProductoId() == null) {
                    throw new ValidationException("El carrito contiene una línea no válida");
                }
                Producto producto = productoRepository.findById(item.getProductoId())
                        .filter(Producto::isActivo)
                        .orElseThrow(() -> new NotFoundException(
                                "Un producto del carrito ya no está disponible"));

                long subtotal = Math.multiplyExact((long) producto.getPrecioCent(), item.getCantidad());
                totalCent = Math.addExact(totalCent, subtotal);
                lineas.add(new LineaPedido(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getReferencia(),
                        producto.getUrlImagen(),
                        producto.getIdVendedor(),
                        producto.getPrecioCent(),
                        item.getCantidad(),
                        subtotal));
            }
        } catch (ArithmeticException error) {
            throw new ValidationException("El importe total del pedido excede el máximo permitido", error);
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setLineas(lineas);
        pedido.setDireccionEnvio(new DireccionEnvioPedido(
                direccionDTO.nombreCompleto().trim(),
                direccionDTO.direccion().trim(),
                direccionDTO.ciudad().trim(),
                direccionDTO.provincia().trim(),
                direccionDTO.codigoPostal().trim(),
                direccionDTO.pais().trim(),
                direccionDTO.telefono().trim()));
        pedido.setMetodoPagoId(metodoPago.getId());
        pedido.setTipoMetodoPago(metodoPago.getTipo());
        pedido.setAliasMetodoPago(metodoPago.getAlias());
        pedido.setReferenciaMetodoPago(metodoPago.getReferencia());
        pedido.setTotalCent(totalCent);
        LocalDateTime ahora = LocalDateTime.now();
        pedido.setEstado(EstadoPedido.EN_PROCESO);
        pedido.setFechaPedido(ahora);
        pedido.setFechaExpiracion(ahora.plusMinutes(PLAZO_PAGO_MINUTOS));
        pedido.setNombreCliente(usuarioRepository.findById(clienteId)
                .map(usuario -> (usuario.getNombre() + " " + usuario.getApellidos()).trim())
                .orElse(direccionDTO.nombreCompleto().trim()));
        return pedido;
    }

    public PedidoResponseDTO pagarPedido(String clienteId, String pedidoId) {
        Pedido pedido = obtenerPedidoPersistido(clienteId, pedidoId);
        if (pedido.getEstado() != EstadoPedido.EN_PROCESO) {
            throw new ConflictException("El pedido ya no está pendiente de pago");
        }
        if (pedido.getFechaExpiracion() == null || !pedido.getFechaExpiracion().isAfter(LocalDateTime.now())) {
            expirarPedido(pedido);
            throw new ConflictException("El plazo para completar la compra ha expirado");
        }

        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setFechaExpiracion(null);
        return convertirAResponseDTO(pedidoRepository.save(pedido));
    }

    public PedidoResponseDTO cancelarPedido(String clienteId, String pedidoId) {
        Pedido pedido = obtenerPedidoPersistido(clienteId, pedidoId);
        Pedido cancelado = cancelarYReponerStock(pedido);
        return convertirAResponseDTO(cancelado);
    }

    public PedidoResponseDTO marcarEnviado(String vendedorId, String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        if (pedido.getEstado() != EstadoPedido.PAGADO
                && pedido.getEstado() != EstadoPedido.TRAMITADO_ENVIADO) {
            throw new ConflictException("El pedido no está disponible para tramitar");
        }

        List<LineaPedido> lineasVendedor = pedido.getLineas().stream()
                .filter(linea -> vendedorId.equals(linea.getIdVendedor()))
                .toList();
        if (lineasVendedor.isEmpty()) {
            throw new NotFoundException("Pedido no encontrado para este vendedor");
        }
        if (lineasVendedor.stream().allMatch(LineaPedido::isEnviado)) {
            throw new ConflictException("Los productos de este vendedor ya se han enviado");
        }
        lineasVendedor.forEach(linea -> linea.setEnviado(true));
        if (pedido.getLineas().stream().allMatch(LineaPedido::isEnviado)) {
            pedido.setEstado(EstadoPedido.TRAMITADO_ENVIADO);
        }
        return convertirAResponseDTO(pedidoRepository.save(pedido), vendedorId);
    }

    public PedidoResponseDTO confirmarEntrega(String clienteId, String pedidoId) {
        Pedido pedido = obtenerPedidoPersistido(clienteId, pedidoId);
        if (pedido.getEstado() != EstadoPedido.TRAMITADO_ENVIADO) {
            throw new ConflictException("El pedido no se puede finalizar hasta que todos los vendedores lo hayan enviado");
        }
        pedido.setEstado(EstadoPedido.FINALIZADO);
        return convertirAResponseDTO(pedidoRepository.save(pedido));
    }

    public List<PedidoResponseDTO> listarPedidosVendedor(String vendedorId, boolean historico) {
        migrarPedidosAntiguos();
        Collection<EstadoPedido> estados = historico
                ? Set.of(EstadoPedido.FINALIZADO, EstadoPedido.CANCELADO)
                : ESTADOS_VISIBLES_VENDEDOR;
        return pedidoRepository.findByLineas_IdVendedorAndEstadoIn(vendedorId, estados).stream()
                .map(pedido -> convertirAResponseDTO(pedido, vendedorId))
                .toList();
    }

    private void migrarPedidosAntiguos() {
        for (Pedido pedido : pedidoRepository.findByEstado(EstadoPedido.RECIBIDO)) {
            boolean actualizado = false;
            for (LineaPedido linea : pedido.getLineas()) {
                if (linea.getIdVendedor() == null) {
                    productoRepository.findById(linea.getProductoId()).ifPresent(producto -> {
                        linea.setIdVendedor(producto.getIdVendedor());
                    });
                    if (linea.getIdVendedor() != null) {
                        actualizado = true;
                    }
                }
            }
            if (actualizado) {
                pedido.setEstado(EstadoPedido.PAGADO);
                pedido.setFechaExpiracion(null);
                if (pedido.getNombreCliente() == null && pedido.getDireccionEnvio() != null) {
                    pedido.setNombreCliente(pedido.getDireccionEnvio().getNombreCompleto());
                }
                pedidoRepository.save(pedido);
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void expirarPedidosPendientesDePago() {
        List<Pedido> vencidos = pedidoRepository.findByEstadoAndFechaExpiracionLessThanEqual(
                EstadoPedido.EN_PROCESO, LocalDateTime.now());
        vencidos.forEach(this::expirarPedido);
    }

    private void expirarPedido(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.EN_PROCESO
                && pedido.getFechaExpiracion() != null
                && !pedido.getFechaExpiracion().isAfter(LocalDateTime.now())) {
            cancelarYReponerStock(pedido);
        }
    }

    private Pedido cancelarYReponerStock(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.EN_PROCESO && pedido.getEstado() != EstadoPedido.PAGADO) {
            throw new ConflictException("El pedido ya no se puede cancelar");
        }
        if (pedido.getLineas().stream().anyMatch(LineaPedido::isEnviado)) {
            throw new ConflictException("No se puede cancelar un pedido que ya ha sido enviado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaExpiracion(null);
        Pedido cancelado = pedidoRepository.save(pedido);
        for (LineaPedido linea : pedido.getLineas()) {
            UpdateResult resultado = mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(linea.getProductoId())),
                    new Update().inc("numStock", linea.getCantidad()),
                    Producto.class);
            if (resultado.getMatchedCount() != 1) {
                throw new BusinessException(
                        "No se pudo restaurar el stock del producto " + linea.getProductoId(),
                        500,
                        "ORDER_STOCK_RESTORE_ERROR");
            }
        }
        return cancelado;
    }

    private Pedido obtenerPedidoPersistido(String clienteId, String pedidoId) {
        return pedidoRepository.findByIdAndClienteId(pedidoId, clienteId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
    }

    private void revertirPedido(Pedido pedido, List<LineaPedido> stockReservado, RuntimeException errorOriginal) {
        RuntimeException errorReversion = null;

        if (pedido != null && pedido.getId() != null) {
            try {
                pedidoRepository.deleteById(pedido.getId());
            } catch (RuntimeException error) {
                errorReversion = combinarErrores(errorReversion, error);
            }
        }

        for (int i = stockReservado.size() - 1; i >= 0; i--) {
            LineaPedido linea = stockReservado.get(i);
            try {
                UpdateResult resultado = mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(linea.getProductoId())),
                        new Update().inc("numStock", linea.getCantidad()),
                        Producto.class);
                if (resultado.getMatchedCount() != 1) {
                    throw new IllegalStateException(
                            "No se pudo restaurar el stock del producto " + linea.getProductoId());
                }
            } catch (RuntimeException error) {
                errorReversion = combinarErrores(errorReversion, error);
            }
        }

        if (errorReversion != null) {
            errorOriginal.addSuppressed(errorReversion);
            throw new BusinessException(
                    "No se pudo completar el pedido ni revertir todos sus cambios",
                    500,
                    "ORDER_ROLLBACK_ERROR",
                    errorOriginal);
        }
    }

    private RuntimeException combinarErrores(RuntimeException errorActual, RuntimeException nuevoError) {
        if (errorActual == null) {
            return nuevoError;
        }
        errorActual.addSuppressed(nuevoError);
        return errorActual;
    }

    private PedidoResponseDTO convertirAResponseDTO(Pedido pedido) {
        return convertirAResponseDTO(pedido, null);
    }

    private PedidoResponseDTO convertirAResponseDTO(Pedido pedido, String vendedorId) {
        List<LineaPedidoResponseDTO> lineas = pedido.getLineas().stream()
                .filter(linea -> vendedorId == null || vendedorId.equals(linea.getIdVendedor()))
                .map(linea -> new LineaPedidoResponseDTO(
                        linea.getProductoId(),
                        linea.getNombre(),
                        linea.getReferencia(),
                        linea.getUrlImagen(),
                        linea.isEnviado(),
                        linea.getPrecioUnitarioCent(),
                        linea.getCantidad(),
                        linea.getSubtotalCent()))
                .toList();
        DireccionEnvioPedido direccion = pedido.getDireccionEnvio();
        DireccionEnvioDTO direccionDTO = new DireccionEnvioDTO(
                direccion.getNombreCompleto(),
                direccion.getDireccion(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getCodigoPostal(),
                direccion.getPais(),
                direccion.getTelefono());
        long totalCent = vendedorId == null
                ? pedido.getTotalCent()
                : lineas.stream().mapToLong(LineaPedidoResponseDTO::subtotalCent).sum();

        return new PedidoResponseDTO(
                pedido.getId(),
                lineas,
                direccionDTO,
                vendedorId == null ? pedido.getMetodoPagoId() : null,
                vendedorId == null ? pedido.getTipoMetodoPago() : null,
                vendedorId == null ? pedido.getAliasMetodoPago() : null,
                vendedorId == null ? pedido.getReferenciaMetodoPago() : null,
                totalCent,
                pedido.getEstado(),
                pedido.getFechaPedido(),
                pedido.getNombreCliente());
    }
}
