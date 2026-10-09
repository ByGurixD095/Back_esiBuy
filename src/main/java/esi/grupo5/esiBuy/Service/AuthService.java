package esi.grupo5.esiBuy.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordValidatorService passwordValidatorService;
    private final LoginAttemptService loginAttempService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder;
    private final AuthFactorService authFactorService;

    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository, PasswordValidatorService passwordValidatorService,
                       LoginAttemptService loginAttempService, EmailService emailService, BCryptPasswordEncoder encoder,
                       AuthFactorService authFactorService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordValidatorService = passwordValidatorService;
        this.loginAttempService = loginAttempService;
        this.emailService = emailService;
        this.encoder = encoder;
        this.authFactorService = authFactorService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) {
        loginAttempService.ensureLoginAllowed(ipAddress);
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.username())
                .orElseThrow(() -> {
                    loginAttempService.registerFailedLogin(ipAddress);
                    throw new AuthException("Credenciales inválidas");
                });

        if (!encoder.matches(loginRequest.password(), usuario.getContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new AuthException("Credenciales inválidas");
        }
        
        if (usuario.isBloqueado() || !usuario.isActivo()) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new ForbiddenException("El usuario no puede iniciar sesión");
        }
        if (usuario.getFechaCambioContrasena() != null && LocalDateTime.now().isAfter(usuario.getFechaCambioContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new PasswordExpiredException("Tu contraseña ha caducado. Debes cambiarla.");
        }
        
        loginAttempService.registerSuccessfulLogin(ipAddress);
        return completarAutenticacion(usuario);
    }

    public LoginResponseDTO completarAutenticacion(Usuario usuario) {
        if (!authFactorService.requiereMfaObligatorio(usuario)) {
            return generarTokens(usuario);
        }
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
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
        return generarTokens(authFactorService.verifyFactors(dto));
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
        } catch (Exception e) { 
            log.error("Error al guardar el refresh token del usuario {}: {}", usuario.getEmail(), e.getMessage());
            throw new BusinessException("Ocurrió un error interno", 500, "INTERNAL_ERROR");
        }
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        return new LoginResponseDTO(token, refreshTokenString, usuario.getRol().toString(), tipoCliente, "SUCCESS", usuario.getEmail());
    }

    public LoginResponseDTO refreshToken(String refreshTokenString) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new AuthException("Token de refresco inválido"));
            
        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new ExpiredTokenException("Refresh token expirado");
        }
        Usuario usuario = refreshTokenEntity.getUsuario(); 
        String nuevoAccessToken = jwtService.generateToken(usuario);
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(nuevoAccessToken, refreshTokenEntity.getToken(), usuario.getRol().toString(), tipoCliente, "SUCCESS", usuario.getEmail());
    }

    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String email = request.email();
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isEmpty()) return; 

        Usuario usuario = optionalUsuario.get();
        String resetToken = UUID.randomUUID().toString();
        usuario.setTokenRecuperacionContrasena(hashToken(resetToken));
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().plusMinutes(5));
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
        usuario.setContrasena(encoder.encode(request.pwd1()));
        usuario.setFechaCambioContrasena(LocalDateTime.now().plusDays(30));
        usuario.getHistorialContrasenas().add(0, usuario.getContrasena());
        if (usuario.getHistorialContrasenas().size() > 5) {
            usuario.getHistorialContrasenas().remove(5);
        }
        usuario.setTokenRecuperacionContrasena(null);
        usuario.setFechaExpiracionTokenRecuperacion(null);
        usuarioRepository.save(usuario);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8))); 
        } catch (Exception e) { 
            throw new BusinessException("Error al generar el hash del token", 500, "INTERNAL_ERROR", e);
        }
    }
}
