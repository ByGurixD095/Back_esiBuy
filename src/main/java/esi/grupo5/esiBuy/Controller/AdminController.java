package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Service.AdminService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone las operaciones de administración de usuarios bajo {@code /admin}. */
@RestController
@RequestMapping("/admin")
public class AdminController {

    /** Servicio de operaciones generales de usuario. */
    private final UserService userService;
    /** Servicio de operaciones reservadas a administradores. */
    private final AdminService adminService;

    /**
     * Crea el controlador administrativo.
     *
     * @param userService servicio de consulta y conversión de usuarios
     * @param adminService servicio de administración de usuarios
     */
    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    /**
     * Lista todos los usuarios.
     *
     * @return usuarios en una respuesta HTTP 200
     */
    @GetMapping("/users")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador del usuario en la ruta
     * @return datos del usuario en una respuesta HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el usuario no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getAdminUserById(id));
    }


    /**
     * Registra un administrador.
     *
     * @param dto datos validados del administrador
     * @return administrador creado con estado HTTP 201
     * @throws esi.grupo5.esiBuy.Exception.ConflictException si el correo ya existe
     * @throws esi.grupo5.esiBuy.Exception.ValidationException si la contraseña no es válida
     */
    @PostMapping
    public ResponseEntity<AdministradorResponseDTO> crearAdministrador(
            @Valid @RequestBody AdministradorRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.crearAdministrador(dto));
    }

    /**
     * Modifica los campos proporcionados de un usuario.
     *
     * @param id identificador del usuario en la ruta
     * @param dto campos parciales que se actualizarán
     * @return respuesta sin cuerpo con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el usuario no existe
     * @throws esi.grupo5.esiBuy.Exception.ValidationException si un campo no supera la validación
     */
    @PatchMapping ("/{id}")
    public ResponseEntity<Void> modificarUsuario (@PathVariable String id, @Valid @RequestBody UserPatchDTO dto) {
        adminService.modificarUsuario(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bloquea una cuenta.
     *
     * @param id identificador del usuario que se bloqueará
     * @return usuario actualizado con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el usuario no existe o está eliminado
     */
    @PatchMapping("/bloquear/{id}")
    public ResponseEntity<UsuarioResponseDTO> bloquearUsuario(@PathVariable String id) {
        return ResponseEntity.ok(adminService.bloquearUsuario(id));
    }

    /**
     * Desbloquea una cuenta.
     *
     * @param id identificador del usuario que se desbloqueará
     * @return usuario actualizado con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el usuario no existe o está eliminado
     */
    @PatchMapping("/desbloquear/{id}")
    public ResponseEntity<UsuarioResponseDTO> desbloquearUsuario(@PathVariable String id) {
        return ResponseEntity.ok(adminService.desbloquearUsuario(id));
    }

    /**
     * Elimina lógicamente a un usuario.
     *
     * @param id identificador del usuario que se eliminará
     * @param usuarioActualId identificador del administrador autenticado
     * @return respuesta sin cuerpo con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.ForbiddenException si se intenta eliminar la propia cuenta
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el usuario no existe o está eliminado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, @AuthenticationPrincipal String usuarioActualId) {
        adminService.eliminarUsuario(id, usuarioActualId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/activar/{id}")
    public ResponseEntity<UsuarioResponseDTO> activarUsuario(@PathVariable String id) {
        return ResponseEntity.ok(adminService.activarUsuario(id));
    }
}