package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import esi.grupo5.esiBuy.Dto.ActualizarCantidadCarritoDTO;
import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Service.CarritoService;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<List<ItemCarritoResponseDTO>> listarCarrito (Authentication authentication) {

        String clienteId = (String) authentication.getPrincipal();

        return ResponseEntity.ok(carritoService.listarCarrito(clienteId));
    }

    @PostMapping("/{productoId}")
    public ResponseEntity<Void> anadirProducto(
            Authentication authentication,
            @PathVariable String productoId) {
        String clienteId = (String) authentication.getPrincipal();
        carritoService.anadirProducto(clienteId, productoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productoId}")
    public ResponseEntity<Void> actualizarCantidad(
            Authentication authentication,
            @PathVariable String productoId,
            @Valid @RequestBody ActualizarCantidadCarritoDTO dto) {
        String clienteId = (String) authentication.getPrincipal();
        carritoService.actualizarCantidad(clienteId, productoId, dto.cantidad());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> eliminarProducto(Authentication authentication,@PathVariable String productoId) {
        String clienteId = (String) authentication.getPrincipal();
        carritoService.eliminarProducto(clienteId, productoId);
        return ResponseEntity.noContent().build();
    }
    
}
