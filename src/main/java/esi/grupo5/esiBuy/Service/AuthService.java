package esi.grupo5.esiBuy.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ExpiredTokenException;
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.PasswordExpiredException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import jakarta.mail.MessagingException;

/** Gestiona inicio de sesión, tokens, MFA y recuperación de contraseñas. */
@Service
public class AuthService {

    /** Duración de los tokens de recuperación, en minutos. */
    private static final int RECOVERY_TOKEN_VALIDITY_MINUTES = 5;
    /** Validez asignada a una contraseña nueva, en días. */
    private static final int PASSWORD_VALIDITY_DAYS = 30;
    /** Número máximo de hashes de contraseña anterior que se conserva. */
    private static final int PASSWORD_HISTORY_LIMIT = 5;
    /** Código común para errores internos del servidor. */
    private static final String SERVER_ERROR_MESSAGE = "INTERNAL_ERROR";
    /** Mensaje para cuentas bloqueadas, eliminadas o inactivas. */
    private static final String ACCOUNT_UNAVAILABLE_MESSAGE = "El usuario no puede iniciar sesión";
    /** Mensaje para contraseñas que han superado su periodo de validez. */
    private static final String PASSWORD_EXPIRED_MESSAGE = "Tu contraseña ha caducado. Debes cambiarla.";
    
    /** Persistencia de cuentas de usuario. */
    private final UsuarioRepository usuarioRepository;
    /** Emisión y validación de JWT. */
    private final JwtService jwtService;
    /** Persistencia de los tokens de refresco. */
    private final RefreshTokenRepository refreshTokenRepository;
    /** Validación y codificación de contraseñas. */
    private final PasswordValidatorService passwordValidatorService;
    /** Control de intentos de acceso por dirección IP. */
    private final LoginAttemptService loginAttempService;
    /** Envío de correos de recuperación. */
    private final EmailService emailService;
    /** Codificador de contraseñas. */
    private final PasswordEncoder passwordEncoder;
    /** Preparación y verificación de factores multifactor. */
    private final AuthFactorService authFactorService;

    /** URL base a la que se añade el token de recuperación. */
    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

    /**
     * Crea el servicio de autenticación.
     *
     * @param usuarioRepository repositorio de cuentas
     * @param jwtService servicio de tokens JWT
     * @param refreshTokenRepository repositorio de tokens de refresco
     * @param passwordValidatorService validador de contraseñas
     * @param loginAttempService control de intentos de acceso
     * @param emailService servicio de correo
     * @param passwordEncoder codificador de contraseñas
     * @param authFactorService servicio de MFA
     */
    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository, PasswordValidatorService passwordValidatorService,
                       LoginAttemptService loginAttempService, EmailService emailService, PasswordEncoder passwordEncoder,
                       AuthFactorService authFactorService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordValidatorService = passwordValidatorService;
        this.loginAttempService = loginAttempService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authFactorService = authFactorService;
    }

    /**
     * Valida credenciales, estado de la cuenta y límites por IP, y continúa el flujo MFA.
     *
     * @param loginRequest credenciales de acceso
     * @param ipAddress dirección IP del cliente
     * @return resultado de autenticación, que puede solicitar completar MFA
     * @throws AuthException si las credenciales no coinciden
     * @throws ForbiddenException si la cuenta no está disponible
     * @throws PasswordExpiredException si la contraseña ha caducado
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) {
        loginAttempService.ensureLoginAllowed(ipAddress);
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.username())
                .orElseThrow(() -> {
                    loginAttempService.registerFailedLogin(ipAddress);
                    throw new AuthException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(loginRequest.password(), usuario.getContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new AuthException("Credenciales inválidas");
        }
        
        try {
            validarEstadoCuenta(usuario);
        } catch (ForbiddenException | PasswordExpiredException e) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw e;
        }
        
        loginAttempService.registerSuccessfulLogin(ipAddress);
        return completarAutenticacion(usuario);
    }

    /**
     * Emite tokens o devuelve el desafío de configuración/verificación MFA correspondiente.
     *
     * @param usuario cuenta cuyas credenciales ya se han validado
     * @return respuesta con tokens o con el siguiente paso de autenticación
     */
    public LoginResponseDTO completarAutenticacion(Usuario usuario) {
        if (!authFactorService.requiereMfaObligatorio(usuario)) {
            return generarTokens(usuario);
        }
        String tipoCliente = obtenerTipoCliente(usuario);
        
        if (!usuario.isMfaConfigurado()) {
            String setupToken = authFactorService.createSetupChallenge(usuario);
            return new LoginResponseDTO(null, null, usuario.getRol().toString(), tipoCliente,
                    "REQUIRES_MFA_SETUP", usuario.getEmail(), setupToken);
        }
        
        if (usuario.getRol().name().equals("ADMINISTRADOR")
                || usuario.getRol().name().equals("VENDEDOR")
                || usuario.is3faActivoCliente()) {
            authFactorService.prepareVerification(usuario);
        }
        return new LoginResponseDTO(null, null, usuario.getRol().toString(), tipoCliente, "REQUIRES_MFA_VERIFICATION", usuario.getEmail());
    }

    /**
     * Verifica los factores enviados y emite tokens si la cuenta supera la autenticación.
     *
     * @param dto correo y códigos de verificación
     * @return respuesta con tokens de acceso y refresco
     */
    public LoginResponseDTO verifyMFA(MfaVerifyRequestDTO dto) {
        Usuario usuario = authFactorService.verifyFactors(dto);
        validarEstadoCuenta(usuario);
        return generarTokens(usuario);
    }

    /**
     * Genera tokens para la cuenta y persiste el token de refresco.
     *
     * @param usuario cuenta autenticada
     * @return respuesta con los tokens emitidos
     * @throws BusinessException si falla el guardado del token de refresco
     */
    public LoginResponseDTO generarTokens(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        String refreshTokenString = jwtService.generateRefreshToken(usuario);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsuario(usuario);
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationSeconds()));
        
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (DataAccessException e) {
            throw new BusinessException("Ocurrió un error interno", 500, SERVER_ERROR_MESSAGE, e);
        }
        return crearRespuestaAutenticacion(usuario, token, refreshTokenString);
    }

    /**
     * Valida el token de refresco almacenado y emite un nuevo token de acceso.
     *
     * @param refreshTokenString token de refresco recibido
     * @return respuesta con token de acceso nuevo y token de refresco existente
     * @throws AuthException si el token no existe o no está asociado a una cuenta
     * @throws ExpiredTokenException si ha expirado
     * @throws ForbiddenException si la cuenta no está disponible
     * @throws PasswordExpiredException si la contraseña ha caducado
     */
    public LoginResponseDTO refreshToken(String refreshTokenString) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new AuthException("Token de refresco inválido"));
            
        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new ExpiredTokenException("Refresh token expirado");
        }
        Usuario usuario = refreshTokenEntity.getUsuario();
        if (usuario == null) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new AuthException("Token de refresco inválido");
        }
        try {
            validarEstadoCuenta(usuario);
        } catch (ForbiddenException | PasswordExpiredException e) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw e;
        }
        String nuevoAccessToken = jwtService.generateToken(usuario);
        
        return crearRespuestaAutenticacion(usuario, nuevoAccessToken, refreshTokenEntity.getToken());
    }

    /**
     * Comprueba que la cuenta esté disponible y que su contraseña no haya caducado.
     *
     * @param usuario cuenta que se validará
     * @throws ForbiddenException si está bloqueada, eliminada o inactiva
     * @throws PasswordExpiredException si la contraseña ha caducado
     */
    private void validarEstadoCuenta(Usuario usuario) {
        if (usuario.isBloqueado() || !usuario.isActivo()) {
            throw new ForbiddenException(ACCOUNT_UNAVAILABLE_MESSAGE);
        }
        if (usuario.getFechaCambioContrasena() != null
                && LocalDateTime.now().isAfter(usuario.getFechaCambioContrasena())) {
            throw new PasswordExpiredException(PASSWORD_EXPIRED_MESSAGE);
        }
    }

    /**
     * Genera y envía un enlace temporal si existe una cuenta para el correo indicado.
     * La operación no revela si la cuenta existe.
     *
     * @param request solicitud con el correo de la cuenta
     * @throws BusinessException si falla el envío del correo
     */
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String email = request.email();
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isEmpty()) return; 

        Usuario usuario = optionalUsuario.get();
        String resetToken = UUID.randomUUID().toString();
        usuario.setTokenRecuperacionContrasena(hashToken(resetToken));
        usuario.setFechaExpiracionTokenRecuperacion(
                LocalDateTime.now().plusMinutes(RECOVERY_TOKEN_VALIDITY_MINUTES));
        usuarioRepository.save(usuario);
        
        try {
            emailService.sendRecoveryEmail(email, usuario.getNombre(), passwordResetUrl + resetToken);
        } catch (MessagingException e) {
            throw new BusinessException("No se ha podido enviar el email de recuperación", 500, SERVER_ERROR_MESSAGE, e);
        }
    }

    /**
     * Revoca todos los tokens de refresco asociados a una cuenta.
     *
     * @param usuarioId identificador de la cuenta cuyos tokens se eliminarán
     */
    public void revocarRefreshTokens(String usuarioId) {
        refreshTokenRepository.deleteByUusuario_Id(usuarioId);
    }

    /**
     * Verifica el token de recuperación y establece la nueva contraseña y su historial.
     *
     * @param request token, nueva contraseña y confirmación
     * @throws ValidationException si las contraseñas no coinciden o el token ha caducado
     * @throws NotFoundException si el token no corresponde a una cuenta
     */
    public void resetPassword(PasswordResetConfirmDTO request) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacionContrasena(hashToken(request.token()))
            .orElseThrow(() -> new NotFoundException("Token de recuperación inválido"));

        if (usuario.getFechaExpiracionTokenRecuperacion() == null || usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token de recuperación expirado");
        }
        
        passwordValidatorService.validatePassword(request.pwd1(), request.pwd2(), usuario.getNombre(), usuario.getEmail(), usuario.getHistorialContrasenas());
        usuario.setContrasena(passwordEncoder.encode(request.pwd1()));
        usuario.setFechaCambioContrasena(LocalDateTime.now().plusDays(PASSWORD_VALIDITY_DAYS));
        usuario.getHistorialContrasenas().add(0, usuario.getContrasena());
        if (usuario.getHistorialContrasenas().size() > PASSWORD_HISTORY_LIMIT) {
            usuario.getHistorialContrasenas().remove(PASSWORD_HISTORY_LIMIT);
        }
        usuario.setTokenRecuperacionContrasena(null);
        usuario.setFechaExpiracionTokenRecuperacion(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Devuelve el tipo de cliente cuando el usuario tiene ese subtipo.
     *
     * @param usuario cuenta que se consulta
     * @return tipo de cliente o {@code null} para el resto de roles
     */
    private String obtenerTipoCliente(Usuario usuario) {
        return usuario instanceof Cliente cliente ? cliente.getTipoCliente().toString() : null;
    }

    /**
     * Crea una respuesta de autenticación completada.
     *
     * @param usuario cuenta autenticada
     * @param accessToken token de acceso
     * @param refreshToken token de refresco
     * @return respuesta con estado {@code SUCCESS}
     */
    private LoginResponseDTO crearRespuestaAutenticacion(
            Usuario usuario, String accessToken, String refreshToken) {
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                usuario.getRol().toString(),
                obtenerTipoCliente(usuario),
                "SUCCESS",
                usuario.getEmail());
    }

    /**
     * Calcula el hash SHA-256 hexadecimal de un token de recuperación.
     *
     * @param token token en texto plano
     * @return hash hexadecimal
     * @throws BusinessException si SHA-256 no está disponible
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8))); 
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("Error al generar el hash del token", 500, SERVER_ERROR_MESSAGE, e);
        }
    }

    
}
