package esi.grupo5.esiBuy.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

/** Gestiona el registro, consulta y actualización de perfiles de usuario. */
@Service
public class UserService {

    /** Mensaje común para búsquedas de usuarios sin resultado. */
    private static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado";

    /** Persistencia de cuentas. */
    private final UsuarioRepository usuarioRepository;
    /** Codificador de contraseñas. */
    private final PasswordEncoder passwordEncoder;
    /** Validador de contraseñas de registro. */
    private final PasswordValidatorService passwordValidatorService;
    /** Servicio que continúa el flujo de autenticación tras el registro. */
    private final AuthService authService;

    /**
     * Crea el servicio de usuarios.
     *
     * @param usuarioRepository repositorio de cuentas
     * @param passwordEncoder codificador de contraseñas
     * @param passwordValidatorService validador de contraseñas
     * @param authService servicio de autenticación posterior al registro
     */
    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       PasswordValidatorService passwordValidatorService, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidatorService = passwordValidatorService;
        this.authService = authService;
    }

    /**
     * Registra un cliente, inicializa su historial de contraseñas y comienza su autenticación.
     *
     * @param dto datos del cliente que se registrará
     * @return respuesta del siguiente paso del flujo de autenticación
     * @throws ConflictException si el correo ya está registrado
     * @throws BusinessException si falla la consulta o persistencia
     */
    @Transactional
    public LoginResponseDTO registrarCliente(ClienteRegistroDTO dto) {
        passwordValidatorService.validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email() ,null);
        validarMayorDeEdad(dto.fechaNacimiento());
        
        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(passwordEncoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .dni(dto.dni())
                .fechaNacimiento(dto.fechaNacimiento())
                .tipoCliente(dto.tipoCliente() != null ? dto.tipoCliente() : TipoCliente.NORMAL)
                .build();

        inicializarHistorialContrasenas(cliente);
        return procesarRegistroUsuario(cliente);
    }

    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isAfter(LocalDate.now(ZoneId.systemDefault()).minusYears(18))) {
            throw new ValidationException("El cliente debe ser mayor de edad para registrarse");
        }
    }

    /**
     * Registra un vendedor, inicializa su historial y comienza su autenticación.
     *
     * @param dto datos del vendedor que se registrará
     * @return respuesta del siguiente paso del flujo de autenticación
     * @throws ConflictException si el correo ya está registrado
     * @throws BusinessException si falla la consulta o persistencia
     */
    @Transactional
    public LoginResponseDTO registrarVendedor(VendedorRegisterRequest dto) {
        passwordValidatorService.validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email() ,null);

        Vendedor vendedor = Vendedor.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(passwordEncoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .imagenPerfil(dto.imagenPerfil())
                .nombreComercial(dto.nombreComercial())
                .cifNif(dto.cifNif())
                .categoriaPrincipalId(dto.categoriaPrincipalId())
                .build();

        inicializarHistorialContrasenas(vendedor);
        return procesarRegistroUsuario(vendedor);
    }

    /**
     * Recupera todos los usuarios y los convierte a DTO de respuesta.
     *
     * @param nombre filtro por nombre
     * @param email filtro por email
     * @param rol filtro por rol
     * @return lista de usuarios representados para la API
     */
    public List<UsuarioResponseDTO> getAllUsers(String nombre, String email, Rol rol) {
    return usuarioRepository.findAll().stream()
            .filter(u -> {
                String nombreCompleto = u.getNombre() + " " + u.getApellidos();
                return nombre == null || nombre.isBlank()
                        || nombreCompleto.toLowerCase().contains(nombre.trim().toLowerCase());
            })
            .filter(u -> email == null || email.isBlank()
                    || u.getEmail().toLowerCase().contains(email.trim().toLowerCase()))
            .filter(u -> rol == null || u.getRol() == rol)
            .map(this::toResponseDto)
            .collect(Collectors.toCollection(ArrayList::new));
}

    /**
     * Recupera una cuenta que no haya sido eliminada lógicamente.
     *
     * @param id identificador del usuario
     * @return DTO del usuario
     * @throws NotFoundException si no existe o está eliminada
     */
    public UsuarioResponseDTO getUserById(String id) {
        return usuarioRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }
    
    /**
     * Recupera una cuenta por identificador, incluyendo cuentas eliminadas lógicamente.
     *
     * @param id identificador del usuario
     * @return DTO del usuario
     * @throws NotFoundException si no existe
     */
    public UsuarioResponseDTO getAdminUserById(String id) {
        return usuarioRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    /**
     * Elimina lógicamente una cuenta, la desactiva y revoca sus tokens de refresco.
     * No permite eliminar al último administrador activo y desbloqueado.
     *
     * @param id identificador de la cuenta que se eliminará
     * @throws NotFoundException si la cuenta no existe o ya está eliminada
     * @throws ForbiddenException si es el último administrador activo y desbloqueado
     */
    public void eliminarCuenta(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));

        if (esAdministradorDisponible(usuario)
                && usuarioRepository.countByRolAndActivoTrueAndBloqueadoFalse(
                        Rol.ADMINISTRADOR) <= 1) {
            throw new ForbiddenException("No se puede eliminar su cuenta.");
        }

        usuarioRepository.delete(usuario);
        authService.revocarRefreshTokens(id);
    }

    /** Determina si una cuenta es un administrador actualmente activo y desbloqueado. */
    private boolean esAdministradorDisponible(Usuario usuario) {
        return usuario.getRol() == Rol.ADMINISTRADOR
                && usuario.isActivo()
                && !usuario.isBloqueado();
    }
    /**
     * Actualiza los campos permitidos del perfil de una cuenta no eliminada.
     *
     * @param id identificador del usuario
     * @param dto campos opcionales de perfil
     * @throws NotFoundException si la cuenta no existe o está eliminada
     * @throws ConflictException si se duplica un campo único
     * @throws BusinessException si falla la persistencia
     */
    @Transactional
    public void modificarMiPerfil(String id, UserSelfUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));

        actualizarDatosComunes(usuario, dto);
        actualizarDatosEspecificos(usuario, dto);
        guardarPerfilActualizado(usuario);
    }

    /**
     * Actualiza los campos comunes cuando se proporciona texto no vacío.
     *
     * @param usuario cuenta que se modificará
     * @param dto valores solicitados
     */
    private void actualizarDatosComunes(Usuario usuario, UserSelfUpdateDTO dto) {
        actualizarSiValido(dto.nombre(), usuario::setNombre);
        actualizarSiValido(dto.apellidos(), usuario::setApellidos);
        actualizarSiValido(dto.telefono(), usuario::setTelefono);
        actualizarSiValido(dto.imagenPerfil(), usuario::setImagenPerfil);
    }

    /**
     * Actualiza los campos específicos de cliente o vendedor que estén presentes.
     *
     * @param usuario cuenta que se modificará
     * @param dto valores de perfil recibidos
     */
    private void actualizarDatosEspecificos(Usuario usuario, UserSelfUpdateDTO dto) {
        if (usuario instanceof Cliente cliente && dto.tipoCliente() != null) {
            cliente.setTipoCliente(dto.tipoCliente());
        }
        if (usuario instanceof Administrador administrador && dto.sede() != null) {
            administrador.setSede(dto.sede());
        }
        if (usuario instanceof Vendedor vendedor) {
            if (dto.nombreComercial() != null && !dto.nombreComercial().isBlank()) {
                validarNombreComercialDisponible(dto.nombreComercial(), vendedor.getId());
            }
            actualizarSiValido(dto.categoriaPrincipalId(), vendedor::setCategoriaPrincipalId);
            actualizarSiValido(dto.nombreComercial(), vendedor::setNombreComercial);
        }
    }

    /**
     * Impide que dos vendedores utilicen el mismo nombre comercial.
     *
     * @param nombreComercial nombre que se quiere asignar
     * @param idUsuario identificador del vendedor que se excluye, o {@code null} durante el registro
     * @throws ConflictException si otro vendedor ya utiliza el nombre
     * @throws BusinessException si falla la consulta de persistencia
     */
    private void validarNombreComercialDisponible(String nombreComercial, String idUsuario) {
        boolean nombreEnUso;
        try {
            nombreEnUso = idUsuario == null
                    ? usuarioRepository.existsByNombreComercial(nombreComercial)
                    : usuarioRepository.existsByNombreComercialAndIdNot(nombreComercial, idUsuario);
        } catch (DataAccessException e) {
            throw new BusinessException(
                    "Error interno al comprobar el nombre comercial", 500, "SERVER_ERROR", e);
        }

        if (nombreEnUso) {
            throw new ConflictException("El nombre comercial ya está en uso");
        }
    }

    /**
     * Persiste el perfil y convierte errores de unicidad y acceso a datos en errores
     * de negocio de la API.
     *
     * @param usuario cuenta modificada
     * @throws ConflictException si se duplica el nombre comercial
     * @throws BusinessException si falla el acceso a datos
     */
    private void guardarPerfilActualizado(Usuario usuario) {
        try {
            usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("El nombre comercial ya está en uso", e);
        } catch (DataAccessException e) {
            throw new BusinessException("Error interno al actualizar el perfil", 500, "SERVER_ERROR", e);
        }
    }

    /**
     * Inicializa el historial de contraseñas con el hash actual.
     *
     * @param usuario cuenta recién creada
     */
    private void inicializarHistorialContrasenas(Usuario usuario) {
        usuario.setHistorialContrasenas(new ArrayList<>(List.of(usuario.getContrasena())));
    }

    /**
     * Comprueba la unicidad del correo, persiste la cuenta y comienza su autenticación.
     *
     * @param usuario nueva cuenta
     * @return respuesta del flujo de autenticación
     * @throws ConflictException si el correo ya está registrado
     * @throws BusinessException si falla la consulta o el guardado
     */
    private LoginResponseDTO procesarRegistroUsuario(Usuario usuario) {
        boolean emailRegistrado;
        try {
            emailRegistrado = usuarioRepository.existsByEmail(usuario.getEmail());
        } catch (DataAccessException e) {
            throw new BusinessException("Error interno al verificar el usuario", 500, "INTERNAL_ERROR", e);
        }
        if (emailRegistrado) {
            throw new ConflictException("El email ya está registrado");
        }

        if (usuario instanceof Vendedor vendedor) {
            validarNombreComercialDisponible(vendedor.getNombreComercial(), null);
        }

        usuario.setActivo(false); // pendiente de activación por un administrador
        Usuario usuarioGuardado;

        try {
            usuarioGuardado = usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            String mensaje = usuario instanceof Vendedor
                    ? "El email o nombre comercial ya está en uso"
                    : "El email ya está registrado";
            throw new ConflictException(mensaje, e);
        } catch (DataAccessException e) {
            throw new BusinessException("Error interno al registrar el usuario", 500, "INTERNAL_ERROR", e);
        }

        // Sin tokens ni MFA: hasta que un administrador la active no puede entrar
        return new LoginResponseDTO(null, null, usuarioGuardado.getRol().toString(), null,
                "PENDING_ACTIVATION", usuarioGuardado.getEmail());
    }

    /**
     * Convierte una entidad al DTO de respuesta correspondiente a su subtipo.
     *
     * @param usuario entidad que se convertirá
     * @return respuesta de cliente, vendedor, administrador o usuario genérico
     */
    public UsuarioResponseDTO toResponseDto(Usuario usuario) {
        if (usuario instanceof Cliente cliente) {
            ClienteResponseDTO.Builder builder = ClienteResponseDTO.builder();
            populateCommonFields(builder, cliente);
            return builder.dni(cliente.getDni())
                    .fechaNacimiento(cliente.getFechaNacimiento())
                    .tipoCliente(cliente.getTipoCliente())
                    .build();
        }

        if (usuario instanceof Vendedor vendedor) {
            VendedorResponseDTO.Builder builder = VendedorResponseDTO.builder();
            populateCommonFields(builder, vendedor);
            return builder.nombreComercial(vendedor.getNombreComercial())
                    .cifNif(vendedor.getCifNif())
                    .categoriaPrincipalId(vendedor.getCategoriaPrincipalId())
                    .build();
        }

        if (usuario instanceof Administrador administrador) {
            return toAdministradorResponseDto(administrador, null);
        }

        UsuarioResponseDTO.UsuarioBuilder builder = UsuarioResponseDTO.usuarioBuilder();
        populateCommonFields(builder, usuario);
        return builder.build();
    }

    /**
     * Construye el DTO específico de una cuenta administradora.
     *
     * @param administrador entidad del administrador
     * @param mensaje mensaje opcional de respuesta
     * @return DTO del administrador
     */
    public AdministradorResponseDTO toAdministradorResponseDto(Administrador administrador, String mensaje) {
        AdministradorResponseDTO.Builder builder = AdministradorResponseDTO.builder();
        populateCommonFields(builder, administrador);
        return builder.sede(administrador.getSede())
                .fechaIncorporacion(administrador.getFechaIncorporacion())
                .mensaje(mensaje)
                .build();
    }

    /**
     * Copia al constructor de respuesta los atributos compartidos de usuario.
     *
     * @param <T> tipo de constructor de respuesta
     * @param builder constructor que recibirá los atributos
     * @param usuario entidad de origen
     */
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
                .fechaAlta(usuario.getFechaAlta())
                .fechaModificacion(usuario.getFechaModificacion())
                .fechaCambioContrasena(usuario.getFechaCambioContrasena())
                .mfaConfigurado(usuario.isMfaConfigurado())
                .dosFactorActivoCliente(usuario.is2faActivoCliente())
                .tresFactorActivoCliente(usuario.is3faActivoCliente());
    }
    
    /**
     * Asigna el texto solo si está informado y no contiene únicamente espacios.
     *
     * @param valor texto solicitado
     * @param setter operación que actualiza el atributo
     */
    private void actualizarSiValido(String valor, Consumer<String> setter) {
        if (valor != null && !valor.isBlank()) {
            setter.accept(valor);
        }
    }

    protected String getNombreComercialById(String vendedorId){
        Usuario user = usuarioRepository.findById(vendedorId)
        .orElseThrow(() -> new NotFoundException("Vendedor no encontrado"));

        if (!(user instanceof Vendedor)){
            throw new NotFoundException("No se encontró el vendedor");
        }

        return ((Vendedor) user).getNombreComercial();
    }
}