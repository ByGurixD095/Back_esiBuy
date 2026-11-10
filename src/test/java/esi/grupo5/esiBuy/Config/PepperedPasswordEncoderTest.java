package esi.grupo5.esiBuy.Config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepperedPasswordEncoderTest {

    @Test
    void acceptsLegacyHashesOnlyForMigrationAndEncodesWithPepper() {
        String password = "Correct#Horse2026";
        String legacyHash = new BCryptPasswordEncoder(10).encode(password);
        PepperedPasswordEncoder encoder = new PepperedPasswordEncoder("0123456789abcdef0123456789abcdef");

        assertTrue(encoder.matches(password, legacyHash));
        assertTrue(encoder.upgradeEncoding(legacyHash));

        String migratedHash = encoder.encode(password);
        assertTrue(encoder.matches(password, migratedHash));
        assertFalse(new BCryptPasswordEncoder(12).matches(password,
                migratedHash.substring("{peppered}".length())));
        assertFalse(encoder.matches("Incorrect#Horse2026", migratedHash));
    }
}
