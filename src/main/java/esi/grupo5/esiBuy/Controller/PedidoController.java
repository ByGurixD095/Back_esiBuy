package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.CrearPedidoRequestDTO;
import esi.grupo5.esiBuy.Dto.PedidoResponseDTO;
import esi.grupo5.esiBuy.Service.PedidoService;
import jakarta.validation.Valid;

/** Endpoints para confirmar el carrito y consultar los pedidos propios. */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(
            Authentication authentication,
            @Valid @RequestBody CrearPedidoRequestDTO dto) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(clienteId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(Authentication authentication) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(pedidoService.listarPedidos(clienteId));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(
            Authentication authentication,
            @PathVariable String pedidoId) {
        String clienteId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(pedidoService.obtenerPedido(clienteId, pedidoId));
    }
}
