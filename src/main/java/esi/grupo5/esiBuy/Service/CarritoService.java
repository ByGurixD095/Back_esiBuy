package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Exception.NotFoundException;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    public List<ItemCarritoResponseDTO> listarCarrito(String clienteId) {
       List<ItemCarrito> items = carritoRepository.findByClienteId(clienteId).map(Carrito::getItems).orElse(Collections.emptyList());

       List<ItemCarritoResponseDTO> resultado = new ArrayList<>();

        for (ItemCarrito item : items) {
            Optional<Producto> productoOptional = productoRepository.findById(item.getProductoId());

            if (productoOptional.isPresent()) {
                Producto producto = productoOptional.get();

            ItemCarritoResponseDTO dto = new ItemCarritoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecioCent(),
                producto.getUrlImagen(),
                item.getCantidad()
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

}
