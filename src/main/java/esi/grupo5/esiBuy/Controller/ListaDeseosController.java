package esi.grupo5.esiBuy.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.ListaDeseosDTO;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Service.ListaDeseosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/listas-deseos")
public class ListaDeseosController {

    private final ListaDeseosService listaDeseosService;

    public ListaDeseosController(ListaDeseosService listaDeseosService) {
        this.listaDeseosService = listaDeseosService;
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<ListaDeseos> crearLista(
            @Valid @RequestBody ListaDeseosDTO dto,
            Authentication authentication) {
        String idCliente = (String) authentication.getPrincipal();
        ListaDeseos lista = listaDeseosService.crearLista(dto.getNombre(), idCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(lista);
    }
}
