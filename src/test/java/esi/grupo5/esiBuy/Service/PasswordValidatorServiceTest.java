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
        service = new PasswordValidatorService();
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    void aceptaUnaContrasenaValida() {
        assertDoesNotThrow(() ->
                service.validarNuevaContrasena("Segura2026!", List.of(), encoder));
    }

    @Test
    void rechazaContrasenaDemasiadoCorta() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarNuevaContrasena("corta", null, encoder));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rechazaContrasenaIncluidaEnLaListaNegra() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarNuevaContrasena("PASSWORD", null, encoder));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rechazaReutilizacionDeContrasena() {
        String oldHash = encoder.encode("Segura2026!");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarNuevaContrasena(
                        "Segura2026!", List.of(oldHash), encoder));

        assertEquals(400, exception.getStatusCode().value());
    }
}
