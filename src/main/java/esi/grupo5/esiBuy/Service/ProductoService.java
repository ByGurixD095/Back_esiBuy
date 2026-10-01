package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
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
                dto.numStock(),
                dto.precioCent(),
                dto.descripcion(),
                dto.categoria(),
                dto.urlImagen()
        );

        return productoRepository.save(producto);
    }
}