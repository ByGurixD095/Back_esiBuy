package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.FavoritoService;

/** Expone los productos favoritos del cliente autenticado. */
@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    /** Lógica de favoritos. */
    private final FavoritoService favoritoService;

    /**
     * Crea el controlador de favoritos.
     *
     * @param favoritoService servicio de favoritos
     */
    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    /**
     * Marca un producto como favorito del cliente autenticado ({@code POST /favoritos/{productoId}}).
     *
     * @param authentication identidad del cliente
     * @param productoId identificador del producto
     * @return respuesta vacía con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el producto no existe o no está activo
     */
    @PostMapping("/{productoId}")
    public ResponseEntity<Void> marcarFavorito(Authentication authentication,
            @PathVariable String productoId) {
        String clienteId = (String) authentication.getPrincipal();
        favoritoService.marcarFavorito(clienteId, productoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista los favoritos del cliente autenticado ({@code GET /favoritos}).
     *
     * @param authentication identidad del cliente
     * @return productos favoritos con estado HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarFavoritos(Authentication authentication) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(favoritoService.listarFavoritos(clienteId));
    }

    /**
     * Quita un producto de los favoritos del cliente ({@code DELETE /favoritos/{productoId}}).
     *
     * @param authentication identidad del cliente
     * @param productoId identificador del producto
     * @return respuesta vacía con estado HTTP 204
     */
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> quitarFavorito(Authentication authentication,
            @PathVariable String productoId) {
        String clienteId = (String) authentication.getPrincipal();
        favoritoService.quitarFavorito(clienteId, productoId);
        return ResponseEntity.noContent().build();
    }
}