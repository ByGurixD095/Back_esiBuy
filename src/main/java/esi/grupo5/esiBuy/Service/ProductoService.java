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
        validarProducto(dto);
        

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

    private void validarProducto(ProductoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (dto.numStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (dto.precioCent() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }

        validarTexto(dto.nombre(), "El nombre");
        validarTexto(dto.referencia(), "La referencia");
        validarTexto(dto.descripcion(), "La descripción");
        validarTexto(dto.categoria(), "La categoría");
        validarTexto(dto.urlImagen(), "La URL de la imagen");
    }

    private void validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " no puede estar vacío");
        }
    }
}