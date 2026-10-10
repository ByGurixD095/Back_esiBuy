package esi.grupo5.esiBuy.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Exception.*;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;

@Service
public class AdminService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder;
    private final PasswordValidatorService passwordValidatorService;
    private final List<UsuarioUpdateStrategy> updateStrategies;
    private final UserService userService;

    public AdminService(UsuarioRepository usuarioRepository, 
                        BCryptPasswordEncoder encoder, 
                        PasswordValidatorService passwordValidatorService,
                        List<UsuarioUpdateStrategy> updateStrategies, UserService userService) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.passwordValidatorService = passwordValidatorService;
        this.updateStrategies = updateStrategies;
        this.userService = userService;
    }

    public void modificarUsuario(String id, UserPatchDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        actualizarDatosComunes(usuario, dto);

        updateStrategies.stream()
                .filter(strategy -> strategy.supports(usuario))
                .findFirst()
                .ifPresent(strategy -> strategy.actualizar(usuario, dto));

        usuarioRepository.save(usuario);
    }

    private void actualizarDatosComunes(Usuario usuario, UserPatchDTO dto) {
        actualizarTexto(dto.nombre(), usuario::setNombre, "El nombre no puede estar vacío");
        actualizarTexto(dto.apellidos(), usuario::setApellidos, "Los apellidos no pueden estar vacíos");

        if (dto.telefono() != null) {
            usuario.setTelefono(dto.telefono());
        }
        if (dto.imagenPerfil() != null) {
            usuario.setImagenPerfil(dto.imagenPerfil());
        }
    }

    private void actualizarTexto(String valor, Consumer<String> setter, String mensajeError) {
        if (valor != null) {
            if (valor.isBlank()) {
                throw new ValidationException(mensajeError);
            }
            setter.accept(valor);
        }
    }

    // --------- BLOQUEAR/DESBLOQUEAR USUARIOS ---------

    public UsuarioResponseDTO bloquearUsuario(String id) {
        return actualizarEstadoBloqueo(id, true);
    }

    public UsuarioResponseDTO desbloquearUsuario(String id) {
        return actualizarEstadoBloqueo(id, false);
    }

    private UsuarioResponseDTO actualizarEstadoBloqueo(String id, boolean bloqueado) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        usuario.setBloqueado(bloqueado);
        usuarioRepository.save(usuario);
        return userService.toResponseDto(usuario);
    }

    public ResponseEntity<AdministradorResponseDTO> crearAdministrador(AdministradorRegistroDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new ConflictException("El email ya está registrado");
        }

        try {
            passwordValidatorService.validatePassword(
                dto.contrasena(),
                new ArrayList<>()
            );
        } catch (ResponseStatusException e) {
            throw new ValidationException("La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }

        Administrador admin = Administrador.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .sede(dto.sede())
                .build();

        Administrador guardado = usuarioRepository.save(admin);

        AdministradorResponseDTO respuesta = userService.toAdministradorResponseDto(
                guardado, "Administrador creado correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    public void eliminarUsuario(String id, String usuarioActualId) {
        if (id.equals(usuarioActualId)) {
            throw new ForbiddenException("No puedes eliminar tu propia cuenta");
        }

        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            long administradores = usuarioRepository.countByRolAndEliminadoFalse(Rol.ADMINISTRADOR);
            if (administradores <= 1) {
                throw new ForbiddenException("No se puede eliminar el último administrador");
            }
        }

        usuario.setEliminado(true);
        usuarioRepository.save(usuario);
    }

}
