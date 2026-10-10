package esi.grupo5.esiBuy.Service;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.ClienteResponseDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.VendedorResponseDTO;
import esi.grupo5.esiBuy.Exception.*;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@Service
public class UserService {

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
        try {
            passwordValidatorService.validatePassword(
                dto.contrasena(),
                new ArrayList<>()
            );
        } catch (ResponseStatusException e) {
            throw new ValidationException("La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
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
           passwordValidatorService.validatePassword(
                dto.contrasena(),
                new ArrayList<>()
            );
        } catch (ResponseStatusException e) {
            throw new ValidationException("La contraseña no cumple con los requisitos de seguridad: " + e.getReason());
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

    // GETTER
    public List<UsuarioResponseDTO> getAllUsers() {
        List<Usuario> usuarios = usuarioRepository.findAllByEliminadoFalse();

        List<UsuarioResponseDTO> usuariosDto = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            usuariosDto.add(toResponseDto(usuario));
        }

        return usuariosDto;
    }



    public UsuarioResponseDTO getUserById(String id) {
    return usuarioRepository.findByIdAndEliminadoFalse(id)
            .map(this::toResponseDto)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    // UPDATE PROFILE
    @Transactional
    public void modificarMiPerfil(String id, UserSelfUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        actualizarSiValido(dto.nombre(), usuario::setNombre);
        actualizarSiValido(dto.apellidos(), usuario::setApellidos);
        actualizarSiValido(dto.telefono(), usuario::setTelefono);
        actualizarSiValido(dto.imagenPerfil(), usuario::setImagenPerfil);

        if (usuario instanceof Cliente cliente && dto.tipoCliente() != null) {
            cliente.setTipoCliente(dto.tipoCliente());
        } else if (usuario instanceof Vendedor vendedor) {
            actualizarSiValido(dto.categoriaPrincipalId(), vendedor::setCategoriaPrincipalId);
            actualizarSiValido(dto.nombreComercial(), vendedor::setNombreComercial);
        }

        try {
            usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("El campo único nombre comercial ya existe");
        } catch (Exception e) {
            throw new BusinessException("Error interno al actualizar el perfil", 500, "INTERNAL_ERROR", e);
        }
    }

    // AUXILIAR METHODS
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