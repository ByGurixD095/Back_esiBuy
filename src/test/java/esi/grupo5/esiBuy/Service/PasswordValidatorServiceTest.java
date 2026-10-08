package esi.grupo5.esiBuy.Service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.web.server.ResponseStatusException;

class PasswordValidatorServiceTest {
    private final PasswordValidatorService service =
            new PasswordValidatorService(new BCryptPasswordEncoder());

    @Test
    void validaContrasenaFuerte() {
        assertDoesNotThrow(() -> service.validatePassword("Strong#2026", List.of()));
    }

    @Test
    void rechazaContrasenaDebil() {
        assertThrows(ResponseStatusException.class,
                () -> service.validatePassword("weak", List.of()));
    }
}
