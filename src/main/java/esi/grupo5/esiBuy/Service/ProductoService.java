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
        validarDto(dto);

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

    private void validarDto(ProductoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO del producto no puede ser nulo");
        }
        if (esInvalido(dto.nombre())) {
            throw new IllegalArgumentException("El nombre del producto no puede ser nulo ni vacío");
        }
        if (esInvalido(dto.referencia())) {
            throw new IllegalArgumentException("La referencia del producto no puede ser nula ni vacía");
        }
        if (esInvalido(dto.descripcion())) {
            throw new IllegalArgumentException("La descripción del producto no puede ser nula ni vacía");
        }
        if (esInvalido(dto.categoria())) {
            throw new IllegalArgumentException("La categoría del producto no puede ser nula ni vacía");
        }
        if (esInvalido(dto.urlImagen())) {
            throw new IllegalArgumentException("La URL de la imagen no puede ser nula ni vacía");
        }
        if (dto.numStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (dto.precioCent() < 0) {
            throw new IllegalArgumentException("El precio en céntimos no puede ser negativo");
        }
    }

    private boolean esInvalido(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}