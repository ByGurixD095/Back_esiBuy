package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Service.CategoriaService;

/** Expone la consulta administrativa de categorías (HU 31). */
@RestController
@RequestMapping("/admin/categorias")
public class CategoriaConsultaController {

    /** Lógica de categorías. */
    private final CategoriaService categoriaService;

    /**
     * Crea el controlador de consulta de categorías.
     *
     * @param categoriaService servicio de categorías
     */
    public CategoriaConsultaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Lista todas las categorías disponibles, ordenadas por nombre.
     *
     * @return 200 con la lista de categorías (vacía si no hay ninguna)
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }
}
