package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.ProductoService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping("/createProduct")
    public ResponseEntity<Producto> crearProducto(@RequestBody ProductoDTO dto) {
        Producto productoCreado = productoService.crearProducto(dto);
        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Producto>> obtenerProductosDisponibles() {
        return ResponseEntity.ok(productoService.obtenerProductosDisponibles());
    }
}