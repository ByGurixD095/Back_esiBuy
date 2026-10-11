package esi.grupo5.esiBuy.Service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
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
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Gestiona factores TOTP, códigos de respaldo y códigos OTP por correo. */
@Service
public class AuthFactorService {

    /** Validez del desafío temporal de configuración MFA, en minutos. */
    private static final int SETUP_TOKEN_VALIDITY_MINUTES = 10;
    /** Validez del código OTP de correo, en minutos. */
    private static final int EMAIL_OTP_VALIDITY_MINUTES = 10;
    /** Cantidad de códigos de respaldo generados al configurar MFA. */
    private static final int BACKUP_CODE_COUNT = 8;
    /** Límite exclusivo para la generación de códigos de respaldo de ocho cifras. */
    private static final int BACKUP_CODE_BOUND = 100000000;
    /** Límite usado para generar códigos OTP de correo. */
    private static final int EMAIL_OTP_BOUND = 999999;
    /** Longitud del vector de inicialización AES-GCM, en bytes. */
    private static final int GCM_IV_LENGTH_BYTES = 12;
    /** Longitud de la etiqueta de autenticación AES-GCM, en bits. */
    private static final int GCM_TAG_LENGTH_BITS = 128;

    /** Clave configurada para cifrar y descifrar secretos TOTP. */
    @Value("${mfa.encryption.key:1234567890123456}")
    private String encryptionKey;

    /** Codificador de hashes de códigos OTP y de respaldo. */
    private final PasswordEncoder passwordEncoder;
    /** Servicio de envío de códigos MFA por correo. */
    private final EmailService emailService;
    /** Persistencia de cuentas y configuración multifactor. */
    private final UsuarioRepository usuarioRepository;

    /** Mensaje común para usuarios inexistentes. */
    private static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    /**
     * Crea el servicio de factores multifactor.
     *
     * @param emailService servicio de envío de correo
     * @param passwordEncoder codificador de códigos de verificación
     * @param usuarioRepository repositorio de usuarios
     */
    public AuthFactorService(EmailService emailService, PasswordEncoder passwordEncoder,
                             UsuarioRepository usuarioRepository) {
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Determina si el rol o la configuración del cliente exige completar MFA.
     *
     * @param usuario usuario cuya política se consulta
     * @return {@code true} si la cuenta debe completar MFA
     */
    public boolean requiereMfaObligatorio(Usuario usuario) {
        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.VENDEDOR) {
            return true;
        }
        return usuario.is2faActivoCliente();
    }

    /**
     * Persiste un desafío temporal de configuración y devuelve su valor sin cifrar.
     *
     * @param usuario cuenta que configurará MFA
     * @return token temporal de configuración
     */
    public String createSetupChallenge(Usuario usuario) {
        String rawToken = UUID.randomUUID().toString();
        usuario.setMfaSetupTokenHash(hash(rawToken));
        usuario.setMfaSetupTokenExpiracion(LocalDateTime.now().plusMinutes(SETUP_TOKEN_VALIDITY_MINUTES));
        usuarioRepository.save(usuario);
        return rawToken;
    }

    /**
     * Busca la cuenta y valida el correo y el desafío temporal de configuración.
     *
     * @param email correo de la cuenta
     * @param setupToken desafío temporal recibido
     * @return usuario autorizado para continuar la configuración
     * @throws NotFoundException si no existe una cuenta con ese correo
     * @throws ExpiredTokenException si el desafío es inválido o ha expirado
     */
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

    /**
     * Genera un secreto TOTP, lo cifra y devuelve su QR y el secreto para configurar
     * la aplicación autenticadora.
     *
     * @param email correo de la cuenta
     * @param setupToken desafío temporal de configuración
     * @return secreto y URI de imagen QR
     */
    public MfaSetupResponseDTO initializeSetup(String email, String setupToken) {
        Usuario usuario = requireSetupAccess(email, setupToken);
        String secret = generarNuevoSecretoTotp();
        usuario.setTotpSecretCifrado(cifrar(secret));
        usuarioRepository.save(usuario);
        return new MfaSetupResponseDTO(generarQrUri(secret, email), secret);
    }

    /**
     * Valida el TOTP inicial, confirma MFA y genera códigos de respaldo.
     *
     * @param dto correo, desafío y código de confirmación
     * @return códigos de respaldo que deben guardarse de forma segura
     * @throws ValidationException si el TOTP es incorrecto
     */
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

    /**
     * Actualiza las opciones de segundo y tercer factor de una cuenta no eliminada.
     *
     * @param userId identificador de usuario
     * @param enable2fa indicador de segundo factor
     * @param enable3fa indicador de tercer factor
     * @throws NotFoundException si no existe una cuenta no eliminada con ese identificador
     */
    public void configureMfa(String userId, boolean enable2fa, boolean enable3fa) {
        Usuario usuario = usuarioRepository.findByIdAndEliminadoFalse(userId)
                .orElseThrow(() -> new NotFoundException(MSG_USUARIO_NO_ENCONTRADO));
        usuario.setIs2faActivoCliente(enable2fa);
        usuario.setIs3faActivoCliente(enable3fa);
        usuarioRepository.save(usuario);
    }

    /**
     * Genera y envía el OTP de correo y persiste su hash y expiración.
     *
     * @param usuario cuenta que realizará la verificación
     */
    public void prepareVerification(Usuario usuario) {
        generarYEnviarEmailOtp(usuario);
        usuarioRepository.save(usuario);
    }

    /**
     * Valida el código TOTP o uno de respaldo y, cuando se exige, el OTP de correo.
     *
     * @param dto correo y códigos que se verificarán
     * @return cuenta verificada
     * @throws NotFoundException si no existe el usuario
     * @throws AuthException si el TOTP y los códigos de respaldo no son válidos
     * @throws ExpiredTokenException si el OTP de correo no es válido o ha expirado
     */
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

    /**
     * Busca un usuario por su correo.
     *
     * @param email correo de la cuenta
     * @return usuario encontrado
     * @throws NotFoundException si no existe una cuenta con ese correo
     */
    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(MSG_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Determina si la cuenta debe verificar también un código enviado por correo.
     *
     * @param usuario cuenta cuya política se consulta
     * @return {@code true} para administradores, vendedores y clientes con 3FA activo
     */
    private boolean requiereOtpEmail(Usuario usuario) {
        return usuario.getRol() == Rol.ADMINISTRADOR
                || usuario.getRol() == Rol.VENDEDOR
                || usuario.is3faActivoCliente();
    }

    /** @return secreto TOTP generado aleatoriamente */
    public String generarNuevoSecretoTotp() {
        return new DefaultSecretGenerator().generate();
    }

    /**
     * Genera una imagen QR PNG codificada como URI de datos para el secreto indicado.
     *
     * @param secretoPlano secreto TOTP que se codificará
     * @param email correo que se muestra como etiqueta
     * @return URI de imagen PNG en Base64
     * @throws BusinessException si no se puede generar el QR
     */
    public String generarQrUri(String secretoPlano, String email) {
        try {
            QrData data = new QrData.Builder()
                    .label(email).secret(secretoPlano).issuer("esiBuy").digits(6).period(30).build();
            byte[] imageData = new ZxingPngQrGenerator().generate(data);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
        } catch (QrGenerationException e) {
            throw new BusinessException("Error generando QR", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Descifra el secreto TOTP de la cuenta y comprueba el código presentado.
     *
     * @param usuario cuenta con el secreto cifrado
     * @param codigoUsuario código TOTP recibido
     * @return {@code true} si el código es válido; {@code false} si no hay secreto
     */
    public boolean verificarCodigoTotp(Usuario usuario, String codigoUsuario) {
        if (usuario.getTotpSecretCifrado() == null) return false;
        String secretoPlano = descifrar(usuario.getTotpSecretCifrado());
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secretoPlano, codigoUsuario);
    }

    /**
     * Compara el código presentado con los códigos de respaldo y consume el que coincida.
     *
     * @param usuario cuenta con los hashes de respaldo
     * @param codigoUsuario código presentado
     * @return {@code true} si se encontró y consumió un código válido
     */
    public boolean verificarCodigoRespaldo(Usuario usuario, String codigoUsuario) {
        Iterator<String> hashes = usuario.getCodigosRespaldoHasheados().iterator();
        while (hashes.hasNext()) {
            String hash = hashes.next();
            if (passwordEncoder.matches(codigoUsuario, hash)) {
                hashes.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Genera códigos de respaldo, guarda sus hashes y devuelve sus valores en claro.
     *
     * @param usuario cuenta a la que pertenecen los códigos
     * @return códigos de respaldo en texto plano
     */
    public List<String> generarCodigosRespaldo(Usuario usuario) {
        SecureRandom random = new SecureRandom();
        List<String> codigosPlanos = IntStream.range(0, BACKUP_CODE_COUNT)
                .mapToObj(i -> String.format("%08d", random.nextInt(BACKUP_CODE_BOUND)))
                .toList();
        usuario.setCodigosRespaldoHasheados(codigosPlanos.stream().map(passwordEncoder::encode).toList());
        return codigosPlanos;
    }

    /**
     * Genera y envía por correo un OTP de seis cifras, guardando el hash y la expiración.
     *
     * @param usuario cuenta destinataria
     * @throws BusinessException si falla el envío del correo
     */
    public void generarYEnviarEmailOtp(Usuario usuario) {
        String otp = String.format("%06d", new SecureRandom().nextInt(EMAIL_OTP_BOUND));
        usuario.setEmailOtpHash(passwordEncoder.encode(otp));
        usuario.setEmailOtpExpiracion(LocalDateTime.now().plusMinutes(EMAIL_OTP_VALIDITY_MINUTES));
        
        String html = "<h2>Código de Verificación</h2><p>Tu código es: <strong>" + otp + "</strong></p><p>Caduca en 10 minutos.</p>";
        try {
            emailService.sendHtmlEmail(usuario.getEmail(), "Código de acceso esiBuy", html);
        } catch (MessagingException e) {
            throw new BusinessException("Error enviando email MFA", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Comprueba la vigencia y coincidencia del OTP enviado por correo.
     *
     * @param usuario cuenta con el hash y la expiración
     * @param codigoUsuario OTP recibido
     * @return {@code true} si el OTP coincide y sigue vigente
     */
    public boolean verificarEmailOtp(Usuario usuario, String codigoUsuario) {
        if (usuario.getEmailOtpExpiracion() == null || usuario.getEmailOtpExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }
        return passwordEncoder.matches(codigoUsuario, usuario.getEmailOtpHash());
    }

    /**
     * Cifra texto mediante AES-GCM y devuelve el vector de inicialización y el texto
     * cifrado autenticado en una cadena Base64.
     *
     * @param textoPlano valor que se cifrará
     * @return vector y contenido cifrado codificados en Base64
     * @throws BusinessException si falla el cifrado
     */
    public String cifrar(String textoPlano) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"), parameterSpec);
            
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes());
            
            byte[] ivYCifrado = new byte[GCM_IV_LENGTH_BYTES + cifrado.length];
            System.arraycopy(iv, 0, ivYCifrado, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(cifrado, 0, ivYCifrado, GCM_IV_LENGTH_BYTES, cifrado.length);
            
            return Base64.getEncoder().encodeToString(ivYCifrado);
        } catch (GeneralSecurityException | IllegalArgumentException | NegativeArraySizeException e) {
            throw new BusinessException("Error cifrando", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Descifra un valor Base64 que contiene el vector de inicialización y el contenido
     * cifrado con AES-GCM.
     *
     * @param textoCifrado contenido cifrado en Base64
     * @return texto original
     * @throws BusinessException si el contenido no se puede descifrar o autenticar
     */
    private String descifrar(String textoCifrado) {
        try {
            byte[] ivYCifrado = Base64.getDecoder().decode(textoCifrado);
            
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cifrado = new byte[ivYCifrado.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(ivYCifrado, 0, iv, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(ivYCifrado, GCM_IV_LENGTH_BYTES, cifrado, 0, cifrado.length);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "AES"), parameterSpec);
            
            return new String(cipher.doFinal(cifrado));
        } catch (GeneralSecurityException | IllegalArgumentException | NegativeArraySizeException e) {
            throw new BusinessException("Error descifrando", 500, "INTERNAL_ERROR", e);
        }
    }

    /**
     * Calcula el hash SHA-256 hexadecimal del valor recibido.
     *
     * @param value texto que se resumirá
     * @return hash hexadecimal
     * @throws BusinessException si el algoritmo SHA-256 no está disponible
     */
    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException("No se pudo generar el hash del desafío MFA", 500, "INTERNAL_ERROR", exception);
        }
    }
}