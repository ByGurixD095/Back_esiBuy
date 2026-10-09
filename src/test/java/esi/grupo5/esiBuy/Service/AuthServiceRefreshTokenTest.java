package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.ExpiredTokenException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.RefreshToken;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private EmailService emailService;
    @Mock private AuthFactorService authFactorService;
    @Mock private BCryptPasswordEncoder encoder;
    @InjectMocks private AuthService service;

    @Test
    void refreshTokenValido_emiteAccessTokenConservandoRefreshToken() {
        Usuario usuario = usuario();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUsuario(usuario);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("new-access-token");

        LoginResponseDTO result = service.refreshToken("refresh-token");

        assertEquals("new-access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("CLIENTE", result.rol());
        assertEquals("NORMAL", result.tipoCliente());
        assertEquals("SUCCESS", result.mfaStatus());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void refreshTokenDesconocido_esRechazado() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> service.refreshToken("missing"));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refreshTokenExpirado_seEliminaYSeRechaza() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired-token");
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(refreshToken));
        when(jwtService.isTokenValid("expired-token")).thenReturn(false);

        assertThrows(ExpiredTokenException.class, () -> service.refreshToken("expired-token"));

        verify(refreshTokenRepository).delete(refreshToken);
        verify(jwtService, never()).generateToken(any());
    }

    private Usuario usuario() {
        return Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("hash")
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .tipoCliente(TipoCliente.NORMAL)
                .build();
    }
}
