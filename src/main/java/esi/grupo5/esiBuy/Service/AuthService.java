package esi.grupo5.esiBuy.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import esi.grupo5.esiBuy.Dto.*;
import esi.grupo5.esiBuy.Exception.*;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import jakarta.mail.MessagingException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int RECOVERY_TOKEN_VALIDITY_MINUTES = 5;
    private static final int PASSWORD_VALIDITY_DAYS = 30;
    private static final int PASSWORD_HISTORY_LIMIT = 5;
    private static final String SERVER_ERROR_MESSAGE = "INTERNAL_ERROR";
    private static final String ACCOUNT_UNAVAILABLE_MESSAGE = "El usuario no puede iniciar sesión";
    private static final String PASSWORD_EXPIRED_MESSAGE = "Tu contraseña ha caducado. Debes cambiarla.";
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordValidatorService passwordValidatorService;
    private final LoginAttemptService loginAttempService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthFactorService authFactorService;

    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

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

    public LoginResponseDTO verifyMFA(MfaVerifyRequestDTO dto) {
        Usuario usuario = authFactorService.verifyFactors(dto);
        validarEstadoCuenta(usuario);
        return generarTokens(usuario);
    }

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
            log.error("Error al guardar el refresh token", e);
            throw new BusinessException("Ocurrió un error interno", 500, SERVER_ERROR_MESSAGE, e);
        }
        return crearRespuestaAutenticacion(usuario, token, refreshTokenString);
    }

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

    private void validarEstadoCuenta(Usuario usuario) {
        if (usuario.isBloqueado() || usuario.isEliminado() || !usuario.isActivo()) {
            throw new ForbiddenException(ACCOUNT_UNAVAILABLE_MESSAGE);
        }
        if (usuario.getFechaCambioContrasena() != null
                && LocalDateTime.now().isAfter(usuario.getFechaCambioContrasena())) {
            throw new PasswordExpiredException(PASSWORD_EXPIRED_MESSAGE);
        }
    }

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
            throw new BusinessException("No se ha podido enviar el email de recuperación", 500, "INTERNAL_ERROR", e);
        }
    }

    public void resetPassword(PasswordResetConfirmDTO request) {
        if (!request.pwd1().equals(request.pwd2())) {
            throw new ValidationException("Las contraseñas no coinciden");
        }
        Usuario usuario = usuarioRepository.findByTokenRecuperacionContrasena(hashToken(request.token()))
            .orElseThrow(() -> new NotFoundException("Token de recuperación inválido"));

        if (usuario.getFechaExpiracionTokenRecuperacion() == null || usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token de recuperación expirado");
        }
        
        passwordValidatorService.validatePassword(request.pwd1(), usuario.getHistorialContrasenas());
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

    private String obtenerTipoCliente(Usuario usuario) {
        return usuario instanceof Cliente cliente ? cliente.getTipoCliente().toString() : null;
    }

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

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8))); 
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("Error al generar el hash del token", 500, SERVER_ERROR_MESSAGE, e);
        }
    }
}
