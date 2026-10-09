package esi.grupo5.esiBuy.Service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import esi.grupo5.esiBuy.Exception.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordValidatorServiceTest {
    private final PasswordValidatorService service =
            new PasswordValidatorService(new BCryptPasswordEncoder());

    @Test
    void validaContrasenaFuerte() {
        assertDoesNotThrow(() -> service.validatePassword("Strong#2026", List.of()));
    }

    @Test
    void rechazaContrasenaDebil() {
        assertThrows(ValidationException.class,
                () -> service.validatePassword("weak", List.of()));
    }
}
