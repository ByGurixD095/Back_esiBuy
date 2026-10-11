package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService service;
    private Usuario user;

    @BeforeEach
    void setUp() {
        service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey",
                "0123456789012345678901234567890123456789012345678901234567890123");
        ReflectionTestUtils.setField(service, "expirationTime", 60_000L);
        ReflectionTestUtils.setField(service, "refreshExpirationTime", 120_000L);
        user = Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("hash")
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .tipoCliente(TipoCliente.NORMAL)
                .build();
        user.setId("user-1");
    }

    @Test
    void generaYValidaTokenDeAccesoConSusClaims() {
        String token = service.generateToken(user);

        assertTrue(service.isTokenValid(token));
        assertEquals("user-1", service.extractId(token));
        assertEquals("CLIENTE", service.extractRol(token));
        assertEquals(60, service.getAccessTokenExpirationSeconds());
    }

    @Test
    void generaRefreshTokenValidoConSuCaducidad() {
        String accessToken = service.generateToken(user);
        String refreshToken = service.generateRefreshToken(user);

        assertTrue(service.isTokenValid(refreshToken));
        assertNotEquals(accessToken, refreshToken);
        assertEquals("user-1", service.extractId(refreshToken));
        assertNull(service.extractRol(refreshToken));
        assertEquals(120, service.getRefreshTokenExpirationSeconds());
    }

    @Test
    void rechazaTokenManipulado() {
        String token = service.generateToken(user);

        assertFalse(service.isTokenValid(token + "alterado"));
    }

    @Test
    void devuelveLaExpiracionEnSegundosTruncandoMilisegundosRestantes() {
        ReflectionTestUtils.setField(service, "expirationTime", 60_999L);
        ReflectionTestUtils.setField(service, "refreshExpirationTime", 120_999L);

        assertEquals(60, service.getAccessTokenExpirationSeconds());
        assertEquals(120, service.getRefreshTokenExpirationSeconds());
    }
}
