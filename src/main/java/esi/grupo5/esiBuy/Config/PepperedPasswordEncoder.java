package esi.grupo5.esiBuy.Config;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** BCrypt con pepper HMAC y compatibilidad temporal para hashes anteriores sin pepper. */
public final class PepperedPasswordEncoder implements PasswordEncoder {

    private static final String PEPPERED_PREFIX = "{peppered}";
    private static final String UNPEPPERED_PREFIX = "{bcrypt}";
    private final BCryptPasswordEncoder delegate;
    private final byte[] pepper;

    public PepperedPasswordEncoder(String pepper) {
        this.delegate = new BCryptPasswordEncoder(12);
        this.pepper = pepper == null ? new byte[0] : pepper.getBytes(StandardCharsets.UTF_8);
        if (this.pepper.length > 0 && this.pepper.length < 32) {
            throw new IllegalArgumentException("PASSWORD_PEPPER must contain at least 32 UTF-8 bytes");
        }
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (pepper.length == 0) return UNPEPPERED_PREFIX + delegate.encode(rawPassword);
        return PEPPERED_PREFIX + delegate.encode(derive(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        if (encodedPassword.startsWith(PEPPERED_PREFIX)) {
            if (pepper.length == 0) return false;
            return delegate.matches(derive(rawPassword), encodedPassword.substring(PEPPERED_PREFIX.length()));
        }
        if (encodedPassword.startsWith(UNPEPPERED_PREFIX)) {
            return delegate.matches(rawPassword, encodedPassword.substring(UNPEPPERED_PREFIX.length()));
        }
        // Existing records were created before peppering. Accept these only so the
        // caller can transparently rehash them after a successful authentication.
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        if (encodedPassword == null) return true;
        if (pepper.length > 0 && !encodedPassword.startsWith(PEPPERED_PREFIX)) return true;
        if (encodedPassword.startsWith(UNPEPPERED_PREFIX)) {
            return delegate.upgradeEncoding(encodedPassword.substring(UNPEPPERED_PREFIX.length()));
        }
        if (!encodedPassword.startsWith(PEPPERED_PREFIX)) return true;
        return delegate.upgradeEncoding(encodedPassword.substring(PEPPERED_PREFIX.length()));
    }

    private String derive(CharSequence rawPassword) {
        if (pepper.length == 0) return rawPassword.toString();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
            byte[] digest = mac.doFinal(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HmacSHA256 is unavailable", exception);
        }
    }
}
