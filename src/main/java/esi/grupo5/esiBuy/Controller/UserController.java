package esi.grupo5.esiBuy.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    
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

    // DELETE 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        //TODO: Implementar
        return ResponseEntity.notFound().build();
    }
}    