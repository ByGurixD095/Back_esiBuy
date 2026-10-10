package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Service.AdminService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    // --------- GET ------------ 
    @GetMapping("/users")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getAdminUserById(id));
    }


    // --------- POST ------------ 
    @PostMapping
    public ResponseEntity<AdministradorResponseDTO> crearAdministrador(
            @Valid @RequestBody AdministradorRegistroDTO dto) {
        return adminService.crearAdministrador(dto);
    }

    // --------- PATCH ------------ 
    @PatchMapping ("/{id}")
    public ResponseEntity<Void> modificarUsuario (@PathVariable String id, @Valid @RequestBody UserPatchDTO dto) {
        adminService.modificarUsuario(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bloquear/{id}")
    public ResponseEntity<UsuarioResponseDTO> bloquearUsuario(@PathVariable String id) {
        return ResponseEntity.ok(adminService.bloquearUsuario(id));
    }

    @PatchMapping("/desbloquear/{id}")
    public ResponseEntity<UsuarioResponseDTO> desbloquearUsuario(@PathVariable String id) {
        return ResponseEntity.ok(adminService.desbloquearUsuario(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, @AuthenticationPrincipal String usuarioActualId) {
        adminService.eliminarUsuario(id, usuarioActualId);
        return ResponseEntity.noContent().build();
    }
}