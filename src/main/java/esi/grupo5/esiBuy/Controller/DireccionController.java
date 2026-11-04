package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Dto.DireccionGuardadaResponseDTO;
import esi.grupo5.esiBuy.Service.DireccionService;
import jakarta.validation.Valid;

/** Gestiona las direcciones de envío guardadas por el cliente autenticado. */
@RestController
@RequestMapping("/users/me/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public ResponseEntity<List<DireccionGuardadaResponseDTO>> listar(Authentication authentication) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(direccionService.listar(clienteId));
    }

    @PostMapping
    public ResponseEntity<DireccionGuardadaResponseDTO> crear(
            Authentication authentication,
            @Valid @RequestBody DireccionEnvioDTO dto) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(clienteId, dto));
    }

    @PutMapping("/{direccionId}")
    public ResponseEntity<DireccionGuardadaResponseDTO> actualizar(
            Authentication authentication,
            @PathVariable String direccionId,
            @Valid @RequestBody DireccionEnvioDTO dto) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(direccionService.actualizar(clienteId, direccionId, dto));
    }

    @DeleteMapping("/{direccionId}")
    public ResponseEntity<Void> eliminar(
            Authentication authentication,
            @PathVariable String direccionId) {
        String clienteId = (String) authentication.getPrincipal();
        direccionService.eliminar(clienteId, direccionId);
        return ResponseEntity.noContent().build();
    }
}
