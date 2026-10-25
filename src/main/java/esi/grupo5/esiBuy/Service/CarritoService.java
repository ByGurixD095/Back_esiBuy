package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Model.ItemCarrito;
import esi.grupo5.esiBuy.Model.Carrito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CarritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    public List<ItemCarrito> listarCarrito(String clienteId) {
       List<ItemCarrito> items = carritoRepository.findByClienteId(clienteId).map(Carrito::getItems).orElse(Collections.emptyList());

        for (ItemCarrito item : items) {
            productoRepository.findById(item.getProductoId());
        }

       return items;
    }

}
