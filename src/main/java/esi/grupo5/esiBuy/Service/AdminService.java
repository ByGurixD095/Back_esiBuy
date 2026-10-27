package esi.grupo5.esiBuy.Service;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;

/** Implementa las operaciones administrativas sobre cuentas de usuario. */
@Service
public class AdminService {
    /** Longitud máxima almacenada para nombre y apellidos en modificaciones. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 30;
    /** Mensaje común para cuentas no encontradas. */
    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    
    /** Persistencia de cuentas. */
    private final UsuarioRepository usuarioRepository;
    /** Codificador de contraseñas de nuevos administradores. */
    private final PasswordEncoder passwordEncoder;
    /** Validador de contraseñas de registro. */
    private final PasswordValidatorService passwordValidatorService;
    /** Estrategias de actualización específicas por tipo de usuario. */
    private final List<UsuarioUpdateStrategy> updateStrategies;
    /** Conversión de usuarios a DTO de respuesta. */
    private final UserService userService;

    /**
     * Crea el servicio administrativo con sus dependencias.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param passwordEncoder codificador de contraseñas
     * @param passwordValidatorService validador de contraseñas
     * @param updateStrategies estrategias por tipo de usuario
     * @param userService servicio de usuarios usado para construir respuestas
     */
    public AdminService(UsuarioRepository usuarioRepository, 
                        PasswordEncoder passwordEncoder,
                        PasswordValidatorService passwordValidatorService,
                        List<UsuarioUpdateStrategy> updateStrategies, UserService userService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidatorService = passwordValidatorService;
        this.updateStrategies = updateStrategies;
        this.userService = userService;
    }

    /**
     * Actualiza los datos comunes y específicos de una cuenta y persiste los cambios.
     *
     * @param id identificador del usuario
     * @param dto campos parciales que se aplicarán
     * @throws NotFoundException si no existe la cuenta
     */
    public void modificarUsuario(String id, UserPatchDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        validarNombreComercial(usuario, dto.nombreComercial());
        actualizarDatosComunes(usuario, dto);

        updateStrategies.stream()
                .filter(strategy -> strategy.supports(usuario))
                .findFirst()
                .ifPresent(strategy -> strategy.actualizar(usuario, dto));

        try {
            usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("El nombre comercial ya está en uso", e);
        }
    }

    /**
     * Comprueba que el nombre comercial modificado no pertenezca a otro vendedor.
     *
     * @param usuario cuenta que se está modificando
     * @param nombreComercial nombre comercial solicitado
     * @throws ConflictException si otro vendedor ya utiliza el nombre
     */
    private void validarNombreComercial(Usuario usuario, String nombreComercial) {
        if (!(usuario instanceof Vendedor vendedor)
                || nombreComercial == null
                || nombreComercial.isBlank()
                || nombreComercial.equals(vendedor.getNombreComercial())) {
            return;
        }

        if (usuarioRepository.existsByNombreComercialAndIdNot(nombreComercial, vendedor.getId())) {
            throw new ConflictException("El nombre comercial ya está en uso");
        }
    }

    /**
     * Actualiza los campos compartidos por todos los tipos de usuario.
     *
     * @param usuario cuenta que se modificará
     * @param dto datos parciales recibidos
     */
    private void actualizarDatosComunes(Usuario usuario, UserPatchDTO dto) {
        actualizarTexto(
                dto.nombre(), usuario::setNombre, "El nombre no puede estar vacío", LONGITUD_MAXIMA_NOMBRE);
        actualizarTexto(
                dto.apellidos(), usuario::setApellidos, "Los apellidos no pueden estar vacíos", LONGITUD_MAXIMA_NOMBRE);

        if (dto.telefono() != null) {
            usuario.setTelefono(dto.telefono());
        }
        if (dto.imagenPerfil() != null) {
            usuario.setImagenPerfil(dto.imagenPerfil());
        }
    }

    /**
     * Valida y asigna un texto opcional, truncándolo al máximo permitido.
     *
     * @param valor texto recibido; si es nulo no se modifica el campo
     * @param setter operación que asigna el texto
     * @param mensajeError mensaje para un valor en blanco
     * @param longitudMaxima longitud máxima que se conservará
     * @throws ValidationException si el texto está en blanco
     */
    private void actualizarTexto(
            String valor, Consumer<String> setter, String mensajeError, int longitudMaxima) {
        if (valor != null) {
            if (valor.isBlank()) {
                throw new ValidationException(mensajeError);
            }
            setter.accept(valor.substring(0, Math.min(valor.length(), longitudMaxima)));
        }
    }

    /**
     * Bloquea una cuenta existente.
     *
     * @param id identificador del usuario
     * @return representación actualizada del usuario
     */
    public UsuarioResponseDTO bloquearUsuario(String id) {
        return actualizarEstadoBloqueo(id, true);
    }

    /**
     * Desbloquea una cuenta existente.
     *
     * @param id identificador del usuario
     * @return representación actualizada del usuario
     */
    public UsuarioResponseDTO desbloquearUsuario(String id) {
        return actualizarEstadoBloqueo(id, false);
    }

    public UsuarioResponseDTO activarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return userService.toResponseDto(usuario);
    }

    /**
     * Cambia y persiste el estado de bloqueo de una cuenta no eliminada.
     *
     * @param id identificador del usuario
     * @param bloqueado nuevo estado de bloqueo
     * @return representación actualizada del usuario
     * @throws NotFoundException si no existe una cuenta no eliminada
     */
    private UsuarioResponseDTO actualizarEstadoBloqueo(String id, boolean bloqueado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        usuario.setBloqueado(bloqueado);
        usuarioRepository.save(usuario);
        return userService.toResponseDto(usuario);
    }

    /**
     * Crea una cuenta administrativa con contraseña validada y codificada.
     *
     * @param dto datos de registro del administrador
     * @return respuesta del administrador creado
     * @throws ConflictException si el correo ya está registrado
     * @throws ValidationException si la contraseña no cumple los requisitos
     */
    public AdministradorResponseDTO crearAdministrador(AdministradorRegistroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        passwordValidatorService.validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email() ,null);

        Administrador admin = Administrador.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(passwordEncoder.encode(dto.contrasena()))
                .sede(dto.sede())
                .imagenPerfil(dto.imagenPerfil())
                .build();

        Administrador guardado;
        try {
            guardado = usuarioRepository.save(admin);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("El email ya está registrado", e);
        }

        return userService.toAdministradorResponseDto(guardado, "Administrador creado correctamente");
    }

    /**
     * Solicita la eliminación lógica de una cuenta y evita que el administrador actual
     * elimine su propia cuenta. La regla común de eliminación se aplica en {@link UserService}.
     *
     * @param id identificador del usuario que se eliminará
     * @param usuarioActualId identificador del administrador que solicita la operación
     * @throws ForbiddenException si el administrador intenta eliminar su propia cuenta o si
     *         la operación dejaría el sistema sin administradores disponibles
     * @throws NotFoundException si la cuenta no existe o ya fue eliminada
     */
    public void eliminarUsuario(String id, String usuarioActualId) {
        if (id.equals(usuarioActualId)) {
            throw new ForbiddenException("No puedes eliminar tu propia cuenta");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
                
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new ForbiddenException("Solo puedes eliminar a otros administradores.");
        }

        userService.eliminarCuenta(id);
    }
}
