package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.ProductoService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // --------- POST ------------ 
    @PostMapping("/createProduct")
    public ResponseEntity<Producto> crearProducto(
            @Valid @RequestBody ProductoDTO dto) {

        return ResponseEntity.ok(productoService.crearProducto(dto));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<Page<Producto>> obtenerProductosDisponibles(
            FiltroCatalogoDTO filtros,
            @PageableDefault(size = 36) Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.obtenerProductosDisponibles(filtros, pageable));
    }
    
}