package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRefreshTokenTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    @Mock
    private LoginAttemptService loginAttemptService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                usuarioRepository,
                jwtService,
                refreshTokenRepository,
                passwordValidatorService,
                loginAttemptService);
    }

    @Test
    void refrescaElAccessTokenManteniendoElRefreshToken() {
        Cliente user = cliente();
        RefreshToken entity = refreshToken("refresh-token", user);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(entity));
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");

        LoginResponseDTO response = userService.refreshToken("refresh-token");

        assertEquals("new-access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("CLIENTE", response.rol());
        assertEquals("NORMAL", response.tipoCliente());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void rechazaRefreshTokenNoEncontrado() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.refreshToken("missing"));

        assertEquals(401, exception.getStatusCode().value());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void eliminaYRechazaRefreshTokenExpirado() {
        RefreshToken entity = refreshToken("expired-token", cliente());
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(entity));
        when(jwtService.isTokenValid("expired-token")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.refreshToken("expired-token"));

        assertEquals(401, exception.getStatusCode().value());
        verify(refreshTokenRepository).delete(entity);
        verify(jwtService, never()).generateToken(any());
    }

    private RefreshToken refreshToken(String token, Cliente user) {
        RefreshToken entity = new RefreshToken();
        entity.setToken(token);
        entity.setUsuario(user);
        return entity;
    }

    private Cliente cliente() {
        return Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("hash")
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build();
    }
}
