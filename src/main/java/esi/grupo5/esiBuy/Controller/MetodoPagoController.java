
package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.MetodoPagoRequestDTO;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;
import esi.grupo5.esiBuy.Service.MetodoPagoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/me/metodoPago")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @PostMapping
    public ResponseEntity<MetodoPagoResponseDTO> registrarMetodoPago(@Valid @RequestBody MetodoPagoRequestDTO dto,Authentication authentication) {

        String clienteId = (String) authentication.getPrincipal();

        MetodoPagoResponseDTO metodoGuardado = metodoPagoService.registrarMetodoPago(clienteId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(metodoGuardado);
    }

    @GetMapping
    public ResponseEntity<List<MetodoPagoResponseDTO>> listarMetodosPago(Authentication authentication) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(metodoPagoService.listarMetodosPago(clienteId));
    }

    @DeleteMapping("/{metodoPagoId}")
    public ResponseEntity<Void> eliminarMetodoPago(@PathVariable String metodoPagoId,Authentication authentication) {
        String clienteId = (String) authentication.getPrincipal();
        metodoPagoService.eliminarMetodoPago(clienteId, metodoPagoId);
        return ResponseEntity.noContent().build();
    }
}
