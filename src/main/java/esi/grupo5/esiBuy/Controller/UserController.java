package esi.grupo5.esiBuy.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import esi.grupo5.esiBuy.Dto.AuthResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Service.UserService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService usuarioService;

    public UserController(UserService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET 
    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        //TODO: Implementar
        return ResponseEntity.ok("In progress");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUserById(@PathVariable int id) {
        //TODO: Implementar
        return ResponseEntity.ok("In progress");
    }

    // POST
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody String name) {
        //TODO: Implemetnar
        return ResponseEntity.ok("Usuario creado: " + name);
    }

    @PostMapping("/clientes")
    public ResponseEntity<AuthResponseDTO> registrarCliente(@Valid @RequestBody ClienteRegistroDTO dto) {
        AuthResponseDTO authResponse = usuarioService.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/register/vendedor")
    public ResponseEntity<AuthResponseDTO> registerVendedor(
            @Valid @RequestBody VendedorRegisterRequest request) {

        AuthResponseDTO response = usuarioService.registerVendedor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // DELETE 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        //TODO: Implementar
        return ResponseEntity.notFound().build();
    }
}    