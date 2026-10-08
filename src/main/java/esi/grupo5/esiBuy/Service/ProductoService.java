package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto crearProducto(ProductoDTO dto) {

        Producto producto = new Producto(
                dto.nombre(),
                dto.referencia(),
                dto.numStock() != null ? dto.numStock() : 0,
                dto.precioCent(),
                dto.descripcion(),
                dto.categoria(),
                dto.urlImagen(),
                dto.descuento(),
                dto.descuentoPremium()
        );

        producto.setActivo(dto.activo() == null || dto.activo());

        return productoRepository.save(producto);
    }
}