package esi.grupo5.esiBuy.Service;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder;
    private final PasswordValidatorService passwordValidatorService;
    private final AuthService authService;

    public UserService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder encoder,
                       PasswordValidatorService passwordValidatorService, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.passwordValidatorService = passwordValidatorService;
        this.authService = authService;
    }

    @Transactional
    public LoginResponseDTO registrarCliente(@Valid ClienteRegistroDTO dto) {
        try {
            passwordValidatorService.passwordIsWeak(dto.contrasena(), new ArrayList<>(), encoder);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }
        
        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .dni(dto.dni())
                .fechaNacimiento(dto.fechaNacimiento())
                .tipoCliente(dto.tipoCliente() != null ? dto.tipoCliente() : TipoCliente.NORMAL)
                .build();

        cliente.setHistorialContrasenas(new ArrayList<>(List.of(cliente.getContrasena())));
        return procesarRegistroUsuario(cliente);
    }

    @Transactional
    public LoginResponseDTO registrarVendedor(@Valid VendedorRegisterRequest dto) {
        try {
           passwordValidatorService.passwordIsWeak(dto.contrasena(), new ArrayList<>(), encoder);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }

        Vendedor vendedor = Vendedor.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .nombreComercial(dto.nombreComercial())
                .cifNif(dto.cifNif())
                .categoriaPrincipalId(dto.categoriaPrincipalId())
                .build();

        vendedor.setHistorialContrasenas(new ArrayList<>(List.of(vendedor.getContrasena())));
        return procesarRegistroUsuario(vendedor);
    }

    private LoginResponseDTO procesarRegistroUsuario(Usuario usuario) {
        try {
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al comprobar la existencia del email {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al verificar el usuario");
        }

        usuario.setActivo(true);
        Usuario usuarioGuardado;

        try {
            usuarioGuardado = usuarioRepository.save(usuario);
        } catch (Exception e) {
            log.error("Error al guardar el nuevo usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al registrar el usuario");
        }

        return authService.generarTokens(usuarioGuardado);
    }

    public List<UserDto> getAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(String id) {
        return usuarioRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    } 

    private UserDto toDto(Usuario usuario) {
        return new UserDto(
            usuario.getId(), usuario.getNombre(), usuario.getApellidos(),
            usuario.getEmail(), usuario.getRol(), usuario.isActivo(), usuario.isBloqueado()
        );
    }
}