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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
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

    @Value("${app.frontend.password-reset-url:http://localhost:4200/reset-password?token=}")
    private String passwordResetUrl;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, 
                       RefreshTokenRepository refreshTokenRepository, PasswordValidatorService passwordValidatorService, 
                       LoginAttemptService loginAttempService, EmailService emailService, BCryptPasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordValidatorService = passwordValidatorService;
        this.loginAttempService = loginAttempService;
        this.emailService = emailService;
        this.encoder = encoder;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest, String ipAddress) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(loginRequest.username());
        if (optionalUsuario.isEmpty() || !encoder.matches(loginRequest.password(), optionalUsuario.get().getContrasena())) {
            loginAttempService.registerFailedLogin(ipAddress);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return generarTokens(optionalUsuario.get());
    }

    public LoginResponseDTO refreshToken(String refreshTokenString) {
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

        if (!jwtService.isTokenValid(refreshTokenEntity.getToken())) {
            try {
                refreshTokenRepository.delete(refreshTokenEntity); 
            } catch (Exception e) {
                log.error("Error al eliminar refresh token expirado para el usuario {}: {}", 
                          refreshTokenEntity.getUsuario().getEmail(), e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        Usuario usuario = refreshTokenEntity.getUsuario(); 
        String nuevoAccessToken = jwtService.generateToken(usuario);
        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        
        return new LoginResponseDTO(nuevoAccessToken, refreshTokenEntity.getToken(), usuario.getRol().toString(), tipoCliente);
    }

    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String email = request.email();
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);

        if (optionalUsuario.isEmpty()) return; 
        
        Usuario usuario = optionalUsuario.get();

        if (usuario.getTokenRecuperacionContrasena() != null && usuario.getFechaExpiracionTokenRecuperacion() != null
                && usuario.getFechaExpiracionTokenRecuperacion().isAfter(LocalDateTime.now())) {
            log.info("El usuario {} ya tiene un token de recuperación válido. Se generará uno nuevo.", email);
        }
        
        String resetToken = UUID.randomUUID().toString();
        usuario.setTokenRecuperacionContrasena(hashToken(resetToken));
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().plusMinutes(5));
        usuarioRepository.save(usuario);

        try {
            emailService.sendRecoveryEmail(email, usuario.getNombre(), passwordResetUrl + resetToken);
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se ha podido enviar el email de recuperación", e);
        }
    }

    public void resetPassword(PasswordResetConfirmDTO request) {
        if (!request.pwd1().equals(request.pwd2())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByTokenRecuperacionContrasena(hashToken(request.token()));
        if (optionalUsuario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación inválido");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getFechaExpiracionTokenRecuperacion() == null || usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación expirado");
        }

        passwordValidatorService.passwordIsWeak(request.pwd1(), usuario.getHistorialContrasenas(), encoder);

        usuario.setContrasena(encoder.encode(request.pwd1()));
        usuario.setFechaCambioContrasena(LocalDateTime.now());
        usuario.getHistorialContrasenas().add(0, usuario.getContrasena());
        if (usuario.getHistorialContrasenas().size() > 5) {
            usuario.getHistorialContrasenas().remove(5);
        }

        usuario.setTokenRecuperacionContrasena(null);
        usuario.setFechaExpiracionTokenRecuperacion(null);
        usuarioRepository.save(usuario);
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno");
        }

        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        return new LoginResponseDTO(token, refreshTokenString, usuario.getRol().toString(), tipoCliente);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); 
        } catch (Exception e) {
            throw new RuntimeException("Error crítico al inicializar SHA-256", e);
        }
    }
}