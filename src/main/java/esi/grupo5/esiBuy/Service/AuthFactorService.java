package esi.grupo5.esiBuy.Service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaBackupCodesDTO;
import esi.grupo5.esiBuy.Dto.MfaConfigRequestDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupConfirmDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AuthFactorService {

    @Value("${mfa.encryption.key:1234567890123456}") // Definir en application.properties (16 bytes)
    private String encryptionKey;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthFactorService(EmailService emailService, UsuarioRepository usuarioRepository,
            JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // --- POLÍTICA POR ROL ---
    public boolean requiereMfaObligatorio(Usuario usuario) {
        // Admin y Vendedor (Creador): 3FA obligatoria. Cliente: Opcional
        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.VENDEDOR) {
            return true;
        }
        return usuario.is2faActivoCliente();
    }

    public LoginResponseDTO completarAutenticacion(Usuario usuario) {
        if (!requiereMfaObligatorio(usuario)) {
            return generarTokens(usuario);
        }

        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        if (!usuario.isMfaConfigurado()) {
            return new LoginResponseDTO(null, null, usuario.getRol().toString(), tipoCliente,
                    "REQUIRES_MFA_SETUP", usuario.getEmail());
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.VENDEDOR
                || usuario.is3faActivoCliente()) {
            generarYEnviarEmailOtp(usuario);
            usuarioRepository.save(usuario);
        }

        return new LoginResponseDTO(null, null, usuario.getRol().toString(), tipoCliente,
                "REQUIRES_MFA_VERIFICATION", usuario.getEmail());
    }

    public LoginResponseDTO verifyMFA(MfaVerifyRequestDTO dto) {
            Usuario usuario = usuarioRepository.findByEmail(dto.email())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            boolean isTotpValid = verificarCodigoTotp(usuario, dto.totpCode())
                    || verificarCodigoRespaldo(usuario, dto.totpCode());
            if (!isTotpValid) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código TOTP inválido");
            }

            if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.VENDEDOR
                    || usuario.is3faActivoCliente()) {
                if (!verificarEmailOtp(usuario, dto.emailOtpCode())) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código de Email inválido o expirado");
                }
                usuario.setEmailOtpHash(null);
                usuario.setEmailOtpExpiracion(null);
                usuarioRepository.save(usuario);
            }

            return generarTokens(usuario);
        }

        public MfaSetupResponseDTO initMfaSetup(String email) {
            Usuario usuario = buscarUsuario(email);
            String secretoPlano = generarNuevoSecretoTotp();
            usuario.setTotpSecretCifrado(cifrar(secretoPlano));
            usuarioRepository.save(usuario);
            return new MfaSetupResponseDTO(generarQrUri(secretoPlano, email), secretoPlano);
        }

        public MfaBackupCodesDTO confirmMfaSetup(MfaSetupConfirmDTO dto) {
            Usuario usuario = buscarUsuario(dto.email());
            if (!verificarCodigoTotp(usuario, dto.code())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El código TOTP es incorrecto. Vuelve a intentarlo.");
            }

            usuario.setMfaConfigurado(true);
            List<String> codigosRespaldo = generarCodigosRespaldo(usuario);
            usuarioRepository.save(usuario);
            return new MfaBackupCodesDTO(codigosRespaldo,
                    "MFA configurado con éxito. Guarda estos códigos en un lugar seguro.");
        }

        public void configureMfa(MfaConfigRequestDTO dto) {
            Usuario usuario = buscarUsuario(dto.email());
            usuario.setIs2faActivoCliente(dto.enable2fa());
            usuario.setIs3faActivoCliente(dto.enable3fa());
            usuarioRepository.save(usuario);
        }

        // --- FACTOR 2: TOTP ---
    public String generarNuevoSecretoTotp() {
        return new DefaultSecretGenerator().generate();
    }

    public String generarQrUri(String secretoPlano, String email) {
        try {
            QrData data = new QrData.Builder()
                    .label(email).secret(secretoPlano).issuer("esiBuy").digits(6).period(30).build();
            byte[] imageData = new ZxingPngQrGenerator().generate(data);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
        } catch (Exception e) { throw new RuntimeException("Error generando QR", e); }
    }

    public boolean verificarCodigoTotp(Usuario usuario, String codigoUsuario) {
        String secretoPlano = descifrar(usuario.getTotpSecretCifrado());
        // DefaultCodeVerifier ya incluye la ventana de tolerancia de ±1 paso (Req. 2.1)
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secretoPlano, codigoUsuario);
    }

    public boolean verificarCodigoRespaldo(Usuario usuario, String codigoUsuario) {
        for (String hash : usuario.getCodigosRespaldoHasheados()) {
            if (encoder.matches(codigoUsuario, hash)) {
                usuario.getCodigosRespaldoHasheados().remove(hash); // Son de un solo uso
                return true;
            }
        }
        return false;
    }

    public List<String> generarCodigosRespaldo(Usuario usuario) {
        SecureRandom random = new SecureRandom();
        List<String> codigosPlanos = IntStream.range(0, 8)
                .mapToObj(i -> String.format("%08d", random.nextInt(100000000)))
                .collect(Collectors.toList());
        usuario.setCodigosRespaldoHasheados(codigosPlanos.stream().map(encoder::encode).collect(Collectors.toList()));
        return codigosPlanos;
    }

    // --- FACTOR 3: EMAIL OTP ---
    public void generarYEnviarEmailOtp(Usuario usuario) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        
        // Hash y validez de 10 min (Req. 3.1)
        usuario.setEmailOtpHash(encoder.encode(otp));
        usuario.setEmailOtpExpiracion(LocalDateTime.now().plusMinutes(10));
        
        String html = "<h2>Código de Verificación</h2><p>Tu código es: <strong>" + otp + "</strong></p><p>Caduca en 10 minutos.</p>";
        try {
            emailService.sendHtmlEmail(usuario.getEmail(), "Código de acceso esiBuy", html);
        } catch (MessagingException e) { throw new RuntimeException("Error enviando email MFA", e); }
    }

    public boolean verificarEmailOtp(Usuario usuario, String codigoUsuario) {
        if (usuario.getEmailOtpExpiracion() == null || usuario.getEmailOtpExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }
        return encoder.matches(codigoUsuario, usuario.getEmailOtpHash());
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private LoginResponseDTO generarTokens(Usuario usuario) {
        String accessToken = jwtService.generateToken(usuario);
        String refreshTokenString = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUsuario(usuario);
        refreshToken.setFechaExpiracion(LocalDateTime.now()
                .plusSeconds(jwtService.getRefreshTokenExpirationSeconds()));
        refreshTokenRepository.save(refreshToken);

        String tipoCliente = (usuario instanceof Cliente cliente) ? cliente.getTipoCliente().toString() : null;
        return new LoginResponseDTO(accessToken, refreshTokenString, usuario.getRol().toString(),
                tipoCliente, "SUCCESS", usuario.getEmail());
    }

    // --- UTILIDADES DE CIFRADO ---
    public String cifrar(String textoPlano) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(textoPlano.getBytes()));
        } catch (Exception e) { throw new RuntimeException("Error cifrando", e); }
    }

    private String descifrar(String textoCifrado) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(textoCifrado)));
        } catch (Exception e) { throw new RuntimeException("Error descifrando", e); }
    }
}