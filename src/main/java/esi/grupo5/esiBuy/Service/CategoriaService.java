package esi.grupo5.esiBuy.Service;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import org.springframework.stereotype.Service;

/** Gestiona la creación y la consulta de categorías. */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public Categoria crearCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new ValidationException("Los datos de la categoría son obligatorios");
        }
        String nombre = categoria.getNombre();
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("El nombre de la categoría no puede estar vacío");
        }

        // Pattern.quote lo trata como texto literal, y IgnoreCase ignora mayúsculas.
        String nombreRegex = "^" + Pattern.quote(nombre.trim()) + "$";
        if (categoriaRepository.existsByNombreRegexIgnoreCase(nombreRegex)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(nombre.trim());
        return categoriaRepository.save(categoria);
    }

    /**
     * Lista todas las categorías ordenadas alfabéticamente por nombre, sin distinguir mayúsculas.
     * La ordenación se hace en memoria porque Mongo no ordena sin distinguir mayúsculas
     * si no se configura una collation.
     *
     * @return categorías ordenadas por nombre; lista vacía si no hay ninguna
     */
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Categoria::getNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public Categoria actualizarCategoria(String id, Categoria cambios) {
        if (cambios == null || cambios.getNombre() == null || cambios.getNombre().isBlank()) {
            throw new ValidationException("El nombre de la categoría no puede estar vacío");
        }

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        String nombre = cambios.getNombre().trim();
        String nombreRegex = "^" + Pattern.quote(nombre) + "$";
        if (categoriaRepository.existsByIdNotAndNombreRegexIgnoreCase(id, nombreRegex)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }
    
    /**
     * Elimina una categoría si existe y ningún producto la usa.
     *
     * @param id identificador de la categoría
     * @throws NotFoundException si la categoría no existe
     * @throws ConflictException si hay productos que usan la categoría
     */
    public void eliminarCategoria(String id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        if (productoRepository.existsByCategoriaId(categoria.getId())) {
            throw new ConflictException(
                    "No se puede eliminar la categoría porque hay productos que la usan");
        }

        categoriaRepository.delete(categoria);
    }
}
