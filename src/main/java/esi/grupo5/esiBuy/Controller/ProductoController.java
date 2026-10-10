package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.ProductoService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(dto));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<Page<Producto>> obtenerProductosDisponibles(
            FiltroCatalogoDTO filtros,
            @PageableDefault(size = 36) Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.obtenerProductosDisponibles(filtros, pageable));
    }

    @GetMapping("/catalogo")
    public ResponseEntity<Page<Producto>> obtenerCatalogoVendedor(
            Authentication authentication,
            @PageableDefault(size = 36) Pageable pageable
    ) {
        String idVendedor = (String) authentication.getPrincipal();

        return ResponseEntity.ok(productoService.obtenerCatalogoVendedor(
                idVendedor,
                pageable.getPageNumber(),
                pageable.getPageSize()
        ));
    }
}