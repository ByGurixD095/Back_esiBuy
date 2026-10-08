package esi.grupo5.esiBuy.Service;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordValidatorService passwordValidatorService;
    private final LoginAttemptService loginAttempService;
    private final EmailService emailService;

    public UserService(UsuarioRepository usuarioRepository, JwtService jwtService, RefreshTokenRepository refreshTokenRepository,
                         PasswordValidatorService passwordValidatorService, LoginAttemptService loginAttempService, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordValidatorService = passwordValidatorService;
        this.loginAttempService = loginAttempService;
        this.emailService = emailService;
    }


    //-------- LOGIN ------------------------------
    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(loginRequest.username());
        if (optionalUsuario.isEmpty() || !encoder.matches(loginRequest.password(), optionalUsuario.get().getContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
         if (optionalUsuario.get().isBloqueado()) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario bloqueado");
    }
        return generarTokens(optionalUsuario.get());
    }

    //-------- TOKENS ------------------------------
    private LoginResponseDTO generarTokens(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        String refreshTokenString = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsuario(usuario);
        long expirationSeconds = jwtService.getRefreshTokenExpirationSeconds();
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(expirationSeconds));
        
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (Exception e) { 
            log.error("Error de base de datos al guardar el refresh token del usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno");
        }

        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(
                token, 
                refreshTokenString, 
                usuario.getRol().toString(), 
                tipoCliente
        );
    }

    public LoginResponseDTO refreshToken(String refreshTokenString) {
        // 1. Comprobamos que el token de refresco exista en la base de datos
        Optional<RefreshToken> optionalRefreshToken;

        try {
            optionalRefreshToken = refreshTokenRepository.findByToken(refreshTokenString);
        } catch (Exception e) {
            log.error("Error de base de datos al buscar el refresh token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno de validación");
        }

        if (optionalRefreshToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresco inválido");
        }

        RefreshToken refreshTokenEntity = optionalRefreshToken.get();

        // 2. Comprobamos que la firma matemática siga siendo válida (que no haya
        // caducado)
        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            try {
                // Lo borramos si ya caducó.
                refreshTokenRepository.delete(refreshTokenEntity); 
            } catch (Exception e) {
                log.error("Error al eliminar refresh token expirado para el usuario {}: {}", 
                          refreshTokenEntity.getUsuario().getEmail(), e.getMessage());
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        // 3. Generamos un nuevo Access Token corto para el usuario
        Usuario usuario = refreshTokenEntity.getUsuario(); 
        String nuevoAccessToken = jwtService.generateToken(usuario);

        // 4. Retornamos el DTO correspondiente según si es Cliente u otro rol
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(
                nuevoAccessToken, 
                refreshTokenEntity.getToken(), 
                usuario.getRol().toString(), 
                tipoCliente
        );

    }

    //-------- REGISTER  ------------------------------

    @Transactional
    public LoginResponseDTO registrarCliente(@Valid ClienteRegistroDTO dto) {
        try{
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
        try{
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

        //-------- MODIFICAR USUARIOS ------------------------------
    public void modificarUsuario(String id, UserPatchDTO dto) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        Usuario usuario = optionalUsuario.get();

        actualizarDatosComunes(usuario, dto);

        if (usuario instanceof Cliente cliente) {
            actualizarDatosCliente(cliente, dto);
        }

        if (usuario instanceof Vendedor vendedor) {
            actualizarDatosVendedor(vendedor, dto);
        }

        if (usuario instanceof Administrador administrador && dto.sede() != null) {
            administrador.setSede(dto.sede());
        }

        usuarioRepository.save(usuario);

    }

    private void actualizarDatosComunes(Usuario usuario, UserPatchDTO dto) {
    if (dto.nombre() != null) {
        if (dto.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
        }
        usuario.setNombre(dto.nombre());
    }

    if (dto.apellidos() != null) {
        if (dto.apellidos().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los apellidos no pueden estar vacíos");
        }
        usuario.setApellidos(dto.apellidos());
    }

    if (dto.telefono() != null) {
        usuario.setTelefono(dto.telefono());
    }

    if (dto.imagenPerfil() != null) {
        usuario.setImagenPerfil(dto.imagenPerfil());
    }

    }

    private void actualizarDatosCliente(Cliente cliente, UserPatchDTO dto) {
    if (dto.dni() != null) {
        cliente.setDni(dto.dni());
    }

    if (dto.fechaNacimiento() != null) {
        if (dto.fechaNacimiento().isAfter(LocalDate.now(ZoneId.systemDefault()).minusYears(18))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente debe ser mayor de edad");
        }
        cliente.setFechaNacimiento(dto.fechaNacimiento());
    }

    if (dto.tipoCliente() != null) {
        cliente.setTipoCliente(dto.tipoCliente());
    }

    }

    private void actualizarDatosVendedor(Vendedor vendedor, UserPatchDTO dto) {
    if (dto.nombreComercial() != null) {
        if (dto.nombreComercial().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre comercial no puede estar vacío");
        }
        vendedor.setNombreComercial(dto.nombreComercial());
    }

    if (dto.cifNif() != null) {
        if (dto.cifNif().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El CIF/NIF no puede estar vacío");
        }
        vendedor.setCifNif(dto.cifNif());
    }

    if (dto.categoriaPrincipalId() != null) {
        if (dto.categoriaPrincipalId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría principal no puede estar vacía");
        }
        vendedor.setCategoriaPrincipalId(dto.categoriaPrincipalId());
    }
    
    }


        //-------- BLOQUEAR/DESBLOQUEAR USUARIOS ---------------------
    public UserDto bloquearUsuario(String id) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        Usuario usuario = optionalUsuario.get();
        usuario.setBloqueado(true);
        usuarioRepository.save(usuario);

        return toDto(usuario);

    }

    public UserDto desbloquearUsuario(String id) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        Usuario usuario = optionalUsuario.get();
        usuario.setBloqueado(false);
        usuarioRepository.save(usuario);
        return toDto(usuario);

    }




        //-------- CREAR ADMINISTRADOR ------------------------------
    public ResponseEntity<AdministradorResponseDTO> crearAdministrador(AdministradorRegistroDTO dto) {
        // Mensaje genérico a propósito: no revelamos si el email ya existe
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha podido crear el administrador");
        }

        Administrador admin = Administrador.builder()
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .email(dto.email())
                .contrasena(encoder.encode(dto.contrasena()))
                .sede(dto.sede())
                .build();

        Administrador guardado = usuarioRepository.save(admin);

        AdministradorResponseDTO respuesta = new AdministradorResponseDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getApellidos(),
                guardado.getEmail(),
                guardado.getSede(),
                guardado.getRol().toString(),
                "Administrador creado correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String email = request.email();
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);

        if (optionalUsuario.isEmpty()) {
            return; 
        }
        Usuario usuario = optionalUsuario.get();

        if (usuario.getTokenRecuperacionContrasena() != null && usuario.getFechaExpiracionTokenRecuperacion() != null
                && usuario.getFechaExpiracionTokenRecuperacion().isAfter(LocalDateTime.now())) {
            log.info("El usuario {} ya tiene un token de recuperación válido. Se generará uno nuevo.", email);
        }
        String resetToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(resetToken);
        usuario.setTokenRecuperacionContrasena(hashedToken);
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().plusMinutes(5));
        usuarioRepository.save(usuario);

        try {
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
            emailService.sendRecoveryEmail(email, usuario.getNombre(), resetLink);
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se ha podido enviar el email de recuperación", e);
        }
    
    }

    public void resetPassword(PasswordResetConfirmDTO request) {
        String token = request.token();
        String pwd1 = request.pwd1();
        String pwd2 = request.pwd2();

 
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByTokenRecuperacionContrasena(hashToken(token));
        if (optionalUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación inválido");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getFechaExpiracionTokenRecuperacion() == null || usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación expirado");
        }

        passwordValidatorService.passwordIsWeak(pwd1, usuario.getHistorialContrasenas(), encoder);

        usuario.setContrasena(encoder.encode(pwd1));
        usuario.setFechaCambioContrasena(LocalDateTime.now());
        usuario.getHistorialContrasenas().add(0, usuario.getContrasena());
        if (usuario.getHistorialContrasenas().size() > 5) {
            usuario.getHistorialContrasenas().remove(5);
        }

        usuario.setTokenRecuperacionContrasena(null);
        usuario.setFechaExpiracionTokenRecuperacion(null);
        usuarioRepository.save(usuario);
    }

    private LoginResponseDTO procesarRegistroUsuario(Usuario usuario) {
        boolean existeEmail;
        try {
            existeEmail = usuarioRepository.existsByEmail(usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al comprobar la existencia del email {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al verificar el usuario");
        }

        if (existeEmail) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        usuario.setActivo(true);

        Usuario usuarioGuardado;
        try {
            usuarioGuardado = usuarioRepository.save(usuario);
        } catch (Exception e) {
            log.error("Error al guardar el nuevo usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al registrar el usuario");
        }

        return generarTokens(usuarioGuardado);
    }

    private UserDto toDto(Usuario usuario) {
        return new UserDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol(),
            usuario.isActivo(),
            usuario.isBloqueado()
        );
    }

    public List<UserDto> getAllUsers() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UserDto> usuariosDto = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            usuariosDto.add(toDto(usuario));
        }

        return usuariosDto;
    }

    public UserDto getUserById(String id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);

        if (usuario.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"
            );
        }

        return toDto(usuario.get());
     } 
      
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // StandardCharsets.UTF_8 asegura que siempre se lean los bytes de la misma forma sin importar el sistema operativo
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            // HexFormat convierte el array de bytes a un String alfanumérico seguro para BBDD (Requiere Java 17+)
            return HexFormat.of().formatHex(hash); 
        } catch (Exception e) {
            throw new RuntimeException("Error crítico de servidor al inicializar el algoritmo SHA-256", e);
        }
    }
    
}