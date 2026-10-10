package esi.grupo5.esiBuy.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.ClienteResponseDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.VendedorResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado";

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

    // REGISTER
    @Transactional
    public LoginResponseDTO registrarCliente(@Valid ClienteRegistroDTO dto) {
        validarContrasenaRegistro(dto.contrasena());
        
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

        inicializarHistorialContrasenas(cliente);
        return procesarRegistroUsuario(cliente);
    }

    @Transactional
    public LoginResponseDTO registrarVendedor(@Valid VendedorRegisterRequest dto) {
        validarContrasenaRegistro(dto.contrasena());

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

        inicializarHistorialContrasenas(vendedor);
        return procesarRegistroUsuario(vendedor);
    }

    // GETTER
    public List<UsuarioResponseDTO> getAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public UsuarioResponseDTO getUserById(String id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    // UPDATE PROFILE
    @Transactional
    public void modificarMiPerfil(String id, UserSelfUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));

        actualizarDatosComunes(usuario, dto);
        actualizarDatosEspecificos(usuario, dto);
        guardarPerfilActualizado(usuario);
    }

    private void actualizarDatosComunes(Usuario usuario, UserSelfUpdateDTO dto) {
        actualizarSiValido(dto.nombre(), usuario::setNombre);
        actualizarSiValido(dto.apellidos(), usuario::setApellidos);
        actualizarSiValido(dto.telefono(), usuario::setTelefono);
        actualizarSiValido(dto.imagenPerfil(), usuario::setImagenPerfil);
    }

    private void actualizarDatosEspecificos(Usuario usuario, UserSelfUpdateDTO dto) {
        if (usuario instanceof Cliente cliente && dto.tipoCliente() != null) {
            cliente.setTipoCliente(dto.tipoCliente());
        } else if (usuario instanceof Vendedor vendedor) {
            actualizarSiValido(dto.categoriaPrincipalId(), vendedor::setCategoriaPrincipalId);
            actualizarSiValido(dto.nombreComercial(), vendedor::setNombreComercial);
        }
    }

    private void guardarPerfilActualizado(Usuario usuario) {
        try {
            usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("El campo único nombre comercial ya existe");
        } catch (Exception e) {
            throw new BusinessException("Error interno al actualizar el perfil", 500, "SERVER_ERROR", e);
        }
    }

    // AUXILIAR METHODS
    private void validarContrasenaRegistro(String contrasena) {
        try {
            passwordValidatorService.validatePassword(contrasena, new ArrayList<>());
        } catch (ResponseStatusException e) {
            throw new ValidationException("La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
        }
    }

    private void inicializarHistorialContrasenas(Usuario usuario) {
        usuario.setHistorialContrasenas(new ArrayList<>(List.of(usuario.getContrasena())));
    }

    private LoginResponseDTO procesarRegistroUsuario(Usuario usuario) {
        try {
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new ConflictException("El email ya está registrado");
            }
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Error interno al verificar el usuario", 500, "INTERNAL_ERROR", e);
        }

        usuario.setActivo(true);
        Usuario usuarioGuardado;

        try {
            usuarioGuardado = usuarioRepository.save(usuario);
        } catch (Exception e) {
            throw new BusinessException("Error interno al registrar el usuario", 500, "INTERNAL_ERROR", e);
        }

        return authService.completarAutenticacion(usuarioGuardado);
    }

    public UsuarioResponseDTO toResponseDto(Usuario usuario) {
        // CLIENTE
        if (usuario instanceof Cliente cliente) {
            ClienteResponseDTO.Builder builder = ClienteResponseDTO.builder();
            populateCommonFields(builder, cliente);
            return builder.dni(cliente.getDni())
                    .fechaNacimiento(cliente.getFechaNacimiento())
                    .tipoCliente(cliente.getTipoCliente())
                    .build();
        }

        // VENDEDOR
        if (usuario instanceof Vendedor vendedor) {
            VendedorResponseDTO.Builder builder = VendedorResponseDTO.builder();
            populateCommonFields(builder, vendedor);
            return builder.nombreComercial(vendedor.getNombreComercial())
                    .cifNif(vendedor.getCifNif())
                    .categoriaPrincipalId(vendedor.getCategoriaPrincipalId())
                    .build();
        }

        // ADMINISTRADOR
        if (usuario instanceof Administrador administrador) {
            return toAdministradorResponseDto(administrador, null);
        }

        //CAMPOS COMUNES USUARIO
        UsuarioResponseDTO.UsuarioBuilder builder = UsuarioResponseDTO.usuarioBuilder();
        populateCommonFields(builder, usuario);
        return builder.build();
    }

    public AdministradorResponseDTO toAdministradorResponseDto(Administrador administrador, String mensaje) {
        AdministradorResponseDTO.Builder builder = AdministradorResponseDTO.builder();
        populateCommonFields(builder, administrador);
        return builder.sede(administrador.getSede())
                .fechaIncorporacion(administrador.getFechaIncorporacion())
                .mensaje(mensaje)
                .build();
    }

    private <T extends UsuarioResponseDTO.Builder<T>> void populateCommonFields(
            UsuarioResponseDTO.Builder<T> builder, Usuario usuario) {
        builder.id(usuario.getId())
                .name(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .imagenPerfil(usuario.getImagenPerfil())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .bloqueado(usuario.isBloqueado())
                .eliminado(usuario.isEliminado())
                .fechaAlta(usuario.getFechaAlta())
                .fechaModificacion(usuario.getFechaModificacion())
                .fechaCambioContrasena(usuario.getFechaCambioContrasena())
                .mfaConfigurado(usuario.isMfaConfigurado())
                .dosFactorActivoCliente(usuario.is2faActivoCliente())
                .tresFactorActivoCliente(usuario.is3faActivoCliente());
    }
    
    private void actualizarSiValido(String valor, Consumer<String> setter) {
        if (valor != null && !valor.isBlank()) {
            setter.accept(valor);
        }
    }
}