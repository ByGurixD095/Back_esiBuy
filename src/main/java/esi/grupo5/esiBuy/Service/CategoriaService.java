package esi.grupo5.esiBuy.Service;

import java.util.regex.Pattern;

import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;
import org.springframework.stereotype.Service;

/** Gestiona la creación de categorías. */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria crearCategoria(Categoria categoria) {
        String nombre = categoria.getNombre();
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("El nombre de la categoría no puede estar vacío");
        }

        // ^ y $ fijan el inicio y final; \\s* admite espacios alrededor del nombre.
        // Pattern.quote lo trata como texto literal, y IgnoreCase ignora mayúsculas.
        String nombreRegex = "^\\s*" + Pattern.quote(nombre.trim()) + "\\s*$";
        if (categoriaRepository.existsByNombreRegexIgnoreCase(nombreRegex)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        return categoriaRepository.save(categoria);
    }
}
