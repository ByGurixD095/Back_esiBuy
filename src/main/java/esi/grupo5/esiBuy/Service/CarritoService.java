package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;

@Service
public class CarritoService {

    private static final String PRODUCTO_NO_DISPONIBLE = "Producto no encontrado o no disponible";

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ItemCarritoResponseDTO> listarCarrito(String clienteId) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId).orElse(null);
        if (carrito == null || carrito.getItems() == null || carrito.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemCarrito> items = new ArrayList<>(carrito.getItems());
        carrito.setItems(items);
        Set<String> ids = items.stream().filter(item -> item != null)
                .map(ItemCarrito::getProductoId).filter(id -> id != null).collect(Collectors.toSet());
        Map<String, Producto> productos = StreamSupport.stream(productoRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toMap(Producto::getId, producto -> producto));
        boolean clientePremium = esClientePremium(clienteId);
        List<ItemCarritoResponseDTO> resultado = new ArrayList<>();
        boolean[] modificado = {false};
        items.removeIf(item -> {
            Producto producto = item == null ? null : productos.get(item.getProductoId());
            if (productoNoDisponible(item, producto, clientePremium)) {
                modificado[0] = true;
                return true;
            }
            if (item.getCantidad() > producto.getNumStock()) {
                item.setCantidad(producto.getNumStock());
                modificado[0] = true;
            }
            resultado.add(new ItemCarritoResponseDTO(producto.getId(), producto.getNombre(),
                    precioConDescuento(producto, clientePremium), producto.getUrlImagen(), item.getCantidad()));
            return false;
        });
        if (modificado[0]) {
            carritoRepository.save(carrito);
        }
        return resultado;
    }

    public void anadirProducto(String clienteId, String productoId) {
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new NotFoundException(PRODUCTO_NO_DISPONIBLE));
        validarAccesoPremium(producto, clienteId);
        if (producto.getNumStock() < 1) {
            throw new ValidationException("El producto no tiene stock disponible");
        }
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> new Carrito(clienteId));
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(producto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            if (item.getCantidad() >= producto.getNumStock()) {
                throw new ValidationException("La cantidad supera el stock disponible");
            }
            item.setCantidad(item.getCantidad() + 1);
        } else {
            carrito.getItems().add(new ItemCarrito(producto.getId(), 1));
        }
        carritoRepository.save(carrito);
    }

    public void actualizarCantidad(String clienteId, String productoId, int cantidad) {
        if (cantidad < 1) {
            throw new ValidationException("La cantidad debe ser al menos 1");
        }

        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new NotFoundException("Carrito no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new NotFoundException(PRODUCTO_NO_DISPONIBLE));
        validarAccesoPremium(producto, clienteId);
        ItemCarrito item = carrito.getItems().stream()
                .filter(actual -> actual.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("El producto no está en el carrito"));

        if (cantidad > producto.getNumStock()) {
            throw new ValidationException("La cantidad supera el stock disponible");
        }

        item.setCantidad(cantidad);
        carritoRepository.save(carrito);
    }

    public void eliminarProducto(String clienteId, String productoId) {

        Carrito carrito = carritoRepository.findByClienteId(clienteId).orElseThrow(() ->
                        new NotFoundException("Carrito no encontrado"));

        boolean eliminado = carrito.getItems().removeIf(item -> item.getProductoId().equals(productoId));
        if (!eliminado) {
            throw new NotFoundException(
                    "El producto no está en el carrito"
            );
        }
        carritoRepository.save(carrito);
    }

    public void vaciarCarrito(String clienteId) {
        carritoRepository.findByClienteId(clienteId).ifPresent(carrito -> {
            carrito.getItems().clear();
            carritoRepository.save(carrito);
        });
    }

    private void validarAccesoPremium(Producto producto, String clienteId) {
        if (producto.isSoloPremium() && !esClientePremium(clienteId)) {
            throw new NotFoundException(PRODUCTO_NO_DISPONIBLE);
        }
    }

    private boolean productoNoDisponible(ItemCarrito item, Producto producto, boolean clientePremium) {
        if (item == null || producto == null) {
            return true;
        }
        if (!producto.isActivo() || producto.getNumStock() < 1 || item.getCantidad() < 1) {
            return true;
        }
        return producto.isSoloPremium() && !clientePremium;
    }

    private boolean esClientePremium(String clienteId) {
        Usuario usuario = usuarioRepository.findById(clienteId).orElse(null);
        return usuario instanceof Cliente cliente && cliente.getTipoCliente() == TipoCliente.PREMIUM;
    }

    private int precioConDescuento(Producto producto, boolean clientePremium) {
        int descuento = clientePremium ? producto.getDescuentoPremium() : producto.getDescuento();
        return (int) ((long) producto.getPrecioCent() * (100 - descuento) / 100);
    }

}
