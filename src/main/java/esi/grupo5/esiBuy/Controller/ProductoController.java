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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone el catálogo y la gestión de productos de los vendedores. */
@RestController
@RequestMapping("/products")
public class ProductoController {

    /** Lógica de catálogo y productos. */
    private final ProductoService productoService;

    /**
     * Crea el controlador de productos.
     *
     * @param productoService servicio de productos
     */
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea un producto asociado al vendedor autenticado.
     *
     * @param authentication identidad del vendedor autenticado
     * @param dto datos validados del producto
     * @return producto creado con estado HTTP 201
     */
    @PostMapping({"", "/createProduct"})
    public ResponseEntity<Producto> crearProducto(
            Authentication authentication,
            @Valid @RequestBody ProductoDTO dto) {
        String idVendedor = (String) authentication.getPrincipal();

        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(dto, idVendedor));
    }

    /**
     * Consulta la página del catálogo público con los filtros y parámetros de paginación
     * de Spring Data; el tamaño predeterminado es 36.
     *
     * @param filtros filtros opcionales del catálogo
     * @param pageable página, tamaño y orden solicitados
     * @return página de productos disponibles con estado HTTP 200
     */
    @GetMapping("/disponibles")
    public ResponseEntity<Page<Producto>> obtenerProductosDisponibles(
            FiltroCatalogoDTO filtros,
            @PageableDefault(size = 36) Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.obtenerProductosDisponibles(filtros, pageable));
    }

    /**
     * Consulta la página de productos del vendedor autenticado.
     *
     * @param authentication identidad del vendedor autenticado
     * @param pageable página solicitada; el tamaño predeterminado es 36
     * @return página del catálogo propio con estado HTTP 200
     */
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

    /**
     * Obtiene un producto que pertenece al vendedor autenticado.
     *
     * @param id identificador del producto en la ruta
     * @param authentication identidad del vendedor propietario
     * @return producto encontrado con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el producto no existe o no pertenece al vendedor
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(
            @PathVariable String id,
            Authentication authentication) {
        String idVendedor = (String) authentication.getPrincipal();
        return ResponseEntity.ok(productoService.obtenerProducto(id, idVendedor));
    }

    /**
     * Sustituye los datos editables de un producto propio.
     *
     * @param id identificador del producto
     * @param authentication identidad del vendedor propietario
     * @param dto nuevos datos validados del producto
     * @return producto actualizado con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el producto no existe o no pertenece al vendedor
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody ProductoDTO dto) {
        String idVendedor = (String) authentication.getPrincipal();
        return ResponseEntity.ok(productoService.actualizarProducto(id, dto, idVendedor));
    }

    /**
     * Desactiva un producto propio.
     *
     * @param id identificador del producto
     * @param authentication identidad del vendedor propietario
     * @return respuesta sin cuerpo con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el producto no existe o no pertenece al vendedor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable String id,
            Authentication authentication) {
        String idVendedor = (String) authentication.getPrincipal();
        productoService.eliminarProducto(id, idVendedor);
        return ResponseEntity.noContent().build();
    }
}