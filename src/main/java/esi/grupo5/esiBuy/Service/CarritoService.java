package esi.grupo5.esiBuy.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this(carritoRepository, productoRepository, null);
    }

    @Autowired
    public CarritoService(
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            @Autowired(required = false) UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ItemCarritoResponseDTO> listarCarrito(String clienteId) {
        List<ItemCarrito> items = carritoRepository.findByClienteId(clienteId)
                .map(Carrito::getItems)
                .orElse(Collections.emptyList());

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> productoIds = items.stream()
                .map(ItemCarrito::getProductoId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, Producto> productosMap = new HashMap<>();
        try {
            Iterable<Producto> productosBatch = productoRepository.findAllById(productoIds);
            if (productosBatch != null) {
                for (Producto p : productosBatch) {
                    if (p != null && p.getId() != null) {
                        productosMap.put(p.getId(), p);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback si el repositorio o mock no implementa findAllById
        }

        // Fallback por producto si falta alguno (p. ej. en tests con mocks puntuales)
        for (String id : productoIds) {
            if (!productosMap.containsKey(id)) {
                productoRepository.findById(id).ifPresent(p -> productosMap.put(p.getId(), p));
            }
        }

        Set<String> vendedorIds = productosMap.values().stream()
                .map(Producto::getIdVendedor)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, String> nombresVendedores = new HashMap<>();
        if (usuarioRepository != null && !vendedorIds.isEmpty()) {
            try {
                Iterable<Usuario> usuarios = usuarioRepository.findAllById(vendedorIds);
                if (usuarios != null) {
                    for (Usuario u : usuarios) {
                        if (u instanceof Vendedor v && v.getNombreComercial() != null && !v.getNombreComercial().isBlank()) {
                            nombresVendedores.put(u.getId(), v.getNombreComercial());
                        } else if (u != null) {
                            String nombre = (u.getNombre() != null ? u.getNombre() : "").trim();
                            String apellidos = (u.getApellidos() != null ? u.getApellidos() : "").trim();
                            String completo = (nombre + " " + apellidos).trim();
                            nombresVendedores.put(u.getId(), completo.isEmpty() ? "Vendedor" : completo);
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback silencioso
            }
        }

        List<ItemCarritoResponseDTO> resultado = new ArrayList<>();
        for (ItemCarrito item : items) {
            Producto producto = productosMap.get(item.getProductoId());
            if (producto != null) {
                String idVendedor = producto.getIdVendedor();
                String nombreVendedor = idVendedor != null
                        ? nombresVendedores.getOrDefault(idVendedor, "Vendedor")
                        : "Vendedor";

                ItemCarritoResponseDTO dto = new ItemCarritoResponseDTO(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getPrecioCent(),
                        producto.getUrlImagen(),
                        item.getCantidad(),
                        idVendedor,
                        nombreVendedor
                );
                resultado.add(dto);
            }
        }

        return resultado;
    }

    public void anadirProducto(String clienteId, String productoId) {
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado o no disponible"));
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> new Carrito(clienteId));
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(producto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
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
                .orElseThrow(() -> new NotFoundException("Producto no encontrado o no disponible"));
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
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new NotFoundException("Carrito no encontrado"));

        boolean eliminado = carrito.getItems().removeIf(item -> item.getProductoId().equals(productoId));
        if (!eliminado) {
            throw new NotFoundException("El producto no está en el carrito");
        }
        carritoRepository.save(carrito);
    }

    public void vaciarCarrito(String clienteId) {
        carritoRepository.findByClienteId(clienteId).ifPresent(carrito -> {
            carrito.getItems().clear();
            carritoRepository.save(carrito);
        });
    }
}
