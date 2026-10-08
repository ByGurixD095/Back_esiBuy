package esi.grupo5.esiBuy.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordValidatorServiceTest {

    private PasswordValidatorService service;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        service = new PasswordValidatorService(encoder);
    }

    @Test
    void aceptaUnaContrasenaValida() {
        assertDoesNotThrow(() ->
                service.validatePassword("Segura2026!", List.of()));
    }

    @Test
    void rechazaContrasenaDemasiadoCorta() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validatePassword("corta", null));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rechazaContrasenaIncluidaEnLaListaNegra() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validatePassword("PASSWORD", null));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rechazaReutilizacionDeContrasena() {
        String oldHash = encoder.encode("Segura2026!");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validatePassword("Segura2026!", List.of(oldHash)));

        assertEquals(400, exception.getStatusCode().value());
    }
}
