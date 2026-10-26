package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Service.CarritoService;


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
    
}
