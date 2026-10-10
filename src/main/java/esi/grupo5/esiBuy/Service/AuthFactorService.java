package esi.grupo5.esiBuy.Service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import esi.grupo5.esiBuy.Dto.MfaBackupCodesDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupConfirmDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ExpiredTokenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthFactorService {

    private static final int SETUP_TOKEN_VALIDITY_MINUTES = 10;
    private static final int EMAIL_OTP_VALIDITY_MINUTES = 10;
    private static final int BACKUP_CODE_COUNT = 8;
    private static final int BACKUP_CODE_BOUND = 100000000;
    private static final int EMAIL_OTP_BOUND = 999999;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    @Value("${mfa.encryption.key:1234567890123456}")
    private String encryptionKey;

    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    private static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    public AuthFactorService(EmailService emailService, BCryptPasswordEncoder encoder,
                             UsuarioRepository usuarioRepository) {
        this.emailService = emailService;
        this.encoder = encoder;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean requiereMfaObligatorio(Usuario usuario) {
        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.VENDEDOR) {
            return true;
        }
        return usuario.is2faActivoCliente();
    }

    public String createSetupChallenge(Usuario usuario) {
        String rawToken = UUID.randomUUID().toString();
        usuario.setMfaSetupTokenHash(hash(rawToken));
        usuario.setMfaSetupTokenExpiracion(LocalDateTime.now().plusMinutes(SETUP_TOKEN_VALIDITY_MINUTES));
        usuarioRepository.save(usuario);
        return rawToken;
    }

    public Usuario requireSetupAccess(String email, String setupToken) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        if (setupToken == null || usuario.getMfaSetupTokenHash() == null
                || usuario.getMfaSetupTokenExpiracion() == null
                || usuario.getMfaSetupTokenExpiracion().isBefore(LocalDateTime.now())
                || !MessageDigest.isEqual(usuario.getMfaSetupTokenHash().getBytes(StandardCharsets.UTF_8),
                        hash(setupToken).getBytes(StandardCharsets.UTF_8))) {
            throw new ExpiredTokenException("Token de configuración MFA inválido o expirado");
        }
        return usuario;
    }

    public MfaSetupResponseDTO initializeSetup(String email, String setupToken) {
        Usuario usuario = requireSetupAccess(email, setupToken);
        String secret = generarNuevoSecretoTotp();
        usuario.setTotpSecretCifrado(cifrar(secret));
        usuarioRepository.save(usuario);
        return new MfaSetupResponseDTO(generarQrUri(secret, email), secret);
    }

    public MfaBackupCodesDTO confirmSetup(MfaSetupConfirmDTO dto) {
        Usuario usuario = requireSetupAccess(dto.email(), dto.setupToken());
        if (!verificarCodigoTotp(usuario, dto.code())) {
            throw new ValidationException("El código TOTP es incorrecto.");
        }
        usuario.setMfaConfigurado(true);
        usuario.setMfaSetupTokenHash(null);
        usuario.setMfaSetupTokenExpiracion(null);
        List<String> backupCodes = generarCodigosRespaldo(usuario);
        usuarioRepository.save(usuario);
        return new MfaBackupCodesDTO(backupCodes,
                "MFA configurado con éxito. Guarda estos códigos en un lugar seguro.");
    }

    public void configureMfa(String userId, boolean enable2fa, boolean enable3fa) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USUARIO_NO_ENCONTRADO));
        usuario.setIs2faActivoCliente(enable2fa);
        usuario.setIs3faActivoCliente(enable3fa);
        usuarioRepository.save(usuario);
    }

    public void prepareVerification(Usuario usuario) {
        generarYEnviarEmailOtp(usuario);
        usuarioRepository.save(usuario);
    }

    public Usuario verifyFactors(MfaVerifyRequestDTO dto) {
        Usuario usuario = buscarUsuarioPorEmail(dto.email());
        boolean totpValid = verificarCodigoTotp(usuario, dto.totpCode())
                || verificarCodigoRespaldo(usuario, dto.totpCode());
        if (!totpValid) {
            throw new AuthException("Código TOTP inválido");
        }

        if (requiereOtpEmail(usuario)) {
            if (!verificarEmailOtp(usuario, dto.emailOtpCode())) {
                throw new ExpiredTokenException("Código de Email inválido o expirado");
            }
            usuario.setEmailOtpHash(null);
            usuario.setEmailOtpExpiracion(null);
            usuarioRepository.save(usuario);
        }
        return usuario;
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MSG_USUARIO_NO_ENCONTRADO));
    }

    private boolean requiereOtpEmail(Usuario usuario) {
        return usuario.getRol() == Rol.ADMINISTRADOR
                || usuario.getRol() == Rol.VENDEDOR
                || usuario.is3faActivoCliente();
    }

    public String generarNuevoSecretoTotp() {
        return new DefaultSecretGenerator().generate();
    }

    public String generarQrUri(String secretoPlano, String email) {
        try {
            QrData data = new QrData.Builder()
                    .label(email).secret(secretoPlano).issuer("esiBuy").digits(6).period(30).build();
            byte[] imageData = new ZxingPngQrGenerator().generate(data);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
        } catch (Exception e) {
            throw new BusinessException("Error generando QR", 500, "INTERNAL_ERROR", e);
        }
    }

    public boolean verificarCodigoTotp(Usuario usuario, String codigoUsuario) {
        if (usuario.getTotpSecretCifrado() == null) return false;
        String secretoPlano = descifrar(usuario.getTotpSecretCifrado());
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secretoPlano, codigoUsuario);
    }

    public boolean verificarCodigoRespaldo(Usuario usuario, String codigoUsuario) {
        Iterator<String> hashes = usuario.getCodigosRespaldoHasheados().iterator();
        while (hashes.hasNext()) {
            String hash = hashes.next();
            if (encoder.matches(codigoUsuario, hash)) {
                hashes.remove();
                return true;
            }
        }
        return false;
    }

    public List<String> generarCodigosRespaldo(Usuario usuario) {
        SecureRandom random = new SecureRandom();
        List<String> codigosPlanos = IntStream.range(0, BACKUP_CODE_COUNT)
                .mapToObj(i -> String.format("%08d", random.nextInt(BACKUP_CODE_BOUND)))
                .toList();
        usuario.setCodigosRespaldoHasheados(codigosPlanos.stream().map(encoder::encode).toList());
        return codigosPlanos;
    }

    public void generarYEnviarEmailOtp(Usuario usuario) {
        String otp = String.format("%06d", new SecureRandom().nextInt(EMAIL_OTP_BOUND));
        usuario.setEmailOtpHash(encoder.encode(otp));
        usuario.setEmailOtpExpiracion(LocalDateTime.now().plusMinutes(EMAIL_OTP_VALIDITY_MINUTES));
        
        String html = "<h2>Código de Verificación</h2><p>Tu código es: <strong>" + otp + "</strong></p><p>Caduca en 10 minutos.</p>";
        try {
            emailService.sendHtmlEmail(usuario.getEmail(), "Código de acceso esiBuy", html);
        } catch (MessagingException e) {
            throw new BusinessException("Error enviando email MFA", 500, "INTERNAL_ERROR", e);
        }
    }

    public boolean verificarEmailOtp(Usuario usuario, String codigoUsuario) {
        if (usuario.getEmailOtpExpiracion() == null || usuario.getEmailOtpExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }
        return encoder.matches(codigoUsuario, usuario.getEmailOtpHash());
    }

    public String cifrar(String textoPlano) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"), parameterSpec);
            
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes());
            
            // Combinar IV + texto cifrado
            byte[] ivYCifrado = new byte[GCM_IV_LENGTH_BYTES + cifrado.length];
            System.arraycopy(iv, 0, ivYCifrado, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(cifrado, 0, ivYCifrado, GCM_IV_LENGTH_BYTES, cifrado.length);
            
            return Base64.getEncoder().encodeToString(ivYCifrado);
        } catch (Exception e) {
            throw new BusinessException("Error cifrando", 500, "INTERNAL_ERROR", e);
        }
    }

    private String descifrar(String textoCifrado) {
        try {
            byte[] ivYCifrado = Base64.getDecoder().decode(textoCifrado);
            
            // Extraer el IV (primeros 12 bytes) y el texto cifrado
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cifrado = new byte[ivYCifrado.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(ivYCifrado, 0, iv, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(ivYCifrado, GCM_IV_LENGTH_BYTES, cifrado, 0, cifrado.length);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"), parameterSpec);
            
            return new String(cipher.doFinal(cifrado));
        } catch (Exception e) {
            throw new BusinessException("Error descifrando", 500, "INTERNAL_ERROR", e);
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BusinessException("No se pudo generar el hash del desafío MFA", 500, "INTERNAL_ERROR", exception);
        }
    }
}