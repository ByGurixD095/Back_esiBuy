package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.ListaDeseosDTO;
import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.ListaDeseosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/listas-deseos")
public class ListaDeseosController {

    private final ListaDeseosService listaDeseosService;

    public ListaDeseosController(ListaDeseosService listaDeseosService) {
        this.listaDeseosService = listaDeseosService;
    }

    @PostMapping
    public ResponseEntity<ListaDeseos> crearLista(
            @Valid @RequestBody ListaDeseosDTO dto,
            Authentication authentication) {
        String idCliente = (String) authentication.getPrincipal();
        ListaDeseos lista = listaDeseosService.crearLista(dto.getNombre(), idCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(lista);
    }

    @GetMapping
    public ResponseEntity<List<ListaDeseos>> obtenerListas (Authentication authentication) {

        String idCliente = (String) authentication.getPrincipal();
        List<ListaDeseos> listas = listaDeseosService.obtenerListas(idCliente);
        
        return ResponseEntity.ok(listas);
    }

    @GetMapping ("/{id}/productos")
    public ResponseEntity<List<Producto>> obtenerProductosLista (@PathVariable String id, Authentication authentication) {

        String idCliente = (String) authentication.getPrincipal();
        List<Producto> productos = listaDeseosService.obtenerProductosLista(id, idCliente);

        return ResponseEntity.ok(productos);
    }

    @PostMapping ("/{id}/productos/{idProducto}")
    public ResponseEntity<ListaDeseos> agregarProductoLista (@PathVariable String id, @PathVariable String idProducto, Authentication authentication) {
        
        String idCliente = (String) authentication.getPrincipal();
        ListaDeseos lista = listaDeseosService.agregarProductoLista(id, idProducto, idCliente);

        return ResponseEntity.ok(lista);
    }
        
    @DeleteMapping("/{id}/productos/{idProducto}")
    public ResponseEntity<Void> eliminarProductoLista(@PathVariable String id,@PathVariable String idProducto,Authentication authentication) {
        String idCliente = (String) authentication.getPrincipal();
        listaDeseosService.eliminarProductoLista(id, idProducto, idCliente);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListaDeseos> modificarLista(@PathVariable String id,@Valid @RequestBody ListaDeseosDTO dto,Authentication authentication) {
        String idCliente = (String) authentication.getPrincipal();
        ListaDeseos lista = listaDeseosService.modificarLista(id, dto.getNombre(), idCliente);
        return ResponseEntity.ok(lista);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLista(@PathVariable String id,Authentication authentication) {
        String idCliente = (String) authentication.getPrincipal();
        listaDeseosService.eliminarLista(id, idCliente);
        return ResponseEntity.noContent().build();
    }


}
