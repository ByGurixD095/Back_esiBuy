package esi.grupo5.esiBuy.Service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordValidatorServiceTest {
    private final PasswordValidatorService service =
            new PasswordValidatorService(new BCryptPasswordEncoder());

    @Test
    void validaContrasenaFuerte() {
        assertDoesNotThrow(() -> service.validatePassword("Strong#2026", "Strong#2026", "Juan", "juan@test.com", List.of()));
    }

    @Test
    void rechazaContrasenaDebil() {
        assertThrows(ValidationException.class,
                () -> service.validatePassword("weak", "weak", "Juan", "juan@test.com", List.of()));
    }

    @Test
    void validatePassword_rechazaCadaRequisitoDeSeguridad() {
        assertThrows(ValidationException.class, () -> service.validatePassword("Strong#2026", "Diferente#2026", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword(null, null, null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("   ", "   ", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("Ab1!xyz", "Ab1!xyz", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword(
                "A".repeat(73) + "a1!", "A".repeat(73) + "a1!", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("Strong #2026", "Strong #2026", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("strong#2026", "strong#2026", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("STRONG#2026", "STRONG#2026", null, null, null));
        assertThrows(ValidationException.class, () -> service.validatePassword("Strong#abcd", "Strong#abcd", null, null, null));
        
        assertDoesNotThrow(() -> service.validatePassword("Password123!", "Password123!", null, null, null));
        
        assertThrows(ValidationException.class, () -> service.validatePassword(
                "Strong#2026", "Strong#2026", null, null, List.of(new BCryptPasswordEncoder().encode("Strong#2026"))));
        
        assertDoesNotThrow(() -> service.validatePassword("Strong#2026", "Strong#2026", null, null, null));
    }

    @Test
    void validatePassword_rechazaDatosPersonales() {
        // Contiene el nombre exacto
        assertThrows(ValidationException.class, 
            () -> service.validatePassword("Alberto#2026", "Alberto#2026", "Alberto", "usuario@test.com", null));
        
        // Contiene el nombre en minúsculas (insensible a mayúsculas)
        assertThrows(ValidationException.class, 
            () -> service.validatePassword("alberto#2026", "alberto#2026", "Alberto", "usuario@test.com", null));

        // Contiene la parte local del email (antes del @)
        assertThrows(ValidationException.class, 
            () -> service.validatePassword("Perez#2026", "Perez#2026", "Juan", "perez@test.com", null));

        // Permite que coincida si el nombre o el prefijo del correo miden 2 caracteres o menos
        assertDoesNotThrow(
            () -> service.validatePassword("Al#2026Password!", "Al#2026Password!", "Al", "al@test.com", null));
    }

    @Test
    void validatePassword_permiteUnHistorialSinCoincidencias() {
        assertDoesNotThrow(() -> service.validatePassword("Strong#2026", "Strong#2026", null, null, List.of(
                new BCryptPasswordEncoder().encode("Different#2026"))));
    }

    @Test
    void hashAndVerifyPassword_deleganAlCodificador() {
        String hash = service.hashPassword("Secret#2026");

        assertTrue(service.verifyPassword("Secret#2026", hash));
        assertThrows(AuthException.class, () -> service.validateOldPassword("wrong", hash));
        assertDoesNotThrow(() -> service.validateOldPassword("Secret#2026", hash));
    }

    @Test
    void passwordChangeAndReset_validanSusCamposObligatorios() {
        assertThrows(ValidationException.class, () -> service.validatePasswordChange(null, "Strong#2026"));
        assertThrows(ValidationException.class, () -> service.validatePasswordChange("current", "weak"));
        assertDoesNotThrow(() -> service.validatePasswordChange("current", "Strong#2026"));

        assertThrows(ValidationException.class,
                () -> service.validatePasswordChangeRequest("current", "Strong#2026", " "));
        assertDoesNotThrow(() -> service.validatePasswordChangeRequest(
                "current", "Strong#2026", "user@example.com"));
        assertThrows(ValidationException.class, () -> service.validatePasswordResetRequest(null));
        assertDoesNotThrow(() -> service.validatePasswordResetRequest("user@example.com"));

        assertThrows(ValidationException.class,
                () -> service.validatePasswordResetConfirmRequest(" ", "Strong#2026"));
        assertThrows(ValidationException.class,
                () -> service.validatePasswordResetConfirmRequest("token", "weak"));
        assertDoesNotThrow(() -> service.validatePasswordResetConfirmRequest("token", "Strong#2026"));

        String hash = service.hashPassword("reset-token");
        assertTrue(service.verifyPasswordResetToken("reset-token", hash));
        assertFalse(service.verifyPasswordResetToken("token", null));
    }

    @Test
    void passwordEncoderErrors_seTraducenAExcepcionDeNegocio() {
        PasswordEncoder brokenEncoder = mock(PasswordEncoder.class);
        when(brokenEncoder.encode("password")).thenThrow(new IllegalStateException("encoder unavailable"));
        when(brokenEncoder.matches("password", "hash"))
                .thenThrow(new IllegalStateException("encoder unavailable"));
        PasswordValidatorService serviceWithBrokenEncoder = new PasswordValidatorService(brokenEncoder);

        assertThrows(BusinessException.class, () -> serviceWithBrokenEncoder.hashPassword("password"));
        assertThrows(BusinessException.class, () -> serviceWithBrokenEncoder.verifyPassword("password", "hash"));
        assertThrows(BusinessException.class,
                () -> serviceWithBrokenEncoder.verifyPasswordResetToken("password", "hash"));
    }
}