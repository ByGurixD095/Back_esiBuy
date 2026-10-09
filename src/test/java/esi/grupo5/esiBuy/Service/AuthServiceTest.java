package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
<<<<<<< HEAD
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
=======
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.PasswordExpiredException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
>>>>>>> 139f0bfa67e3f22919e97df783e7ecffbe326765
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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
    void loginSinMfa_obtieneTokensYLimpiaIntentosFallidos() {
        Cliente usuario = cliente();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", "hash")).thenReturn(true);
        when(authFactorService.requiereMfaObligatorio(usuario)).thenReturn(false);
        when(jwtService.generateToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);

        LoginResponseDTO result = service.login(
                new LoginRequestDTO(usuario.getEmail(), "Password#1"), "127.0.0.1");

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertEquals("CLIENTE", result.rol());
        assertEquals("NORMAL", result.tipoCliente());
        assertEquals("SUCCESS", result.mfaStatus());
        verify(loginAttemptService).registerSuccessfulLogin("127.0.0.1");
        verify(refreshTokenRepository).save(any());
    }

    @Test
<<<<<<< HEAD
    void loginUsuarioBloqueado_Rechazado() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("user@test.com");
        usuario.setContrasena(new BCryptPasswordEncoder().encode("Password#1"));
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setBloqueado(true);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", usuario.getContrasena())).thenReturn(true);

        LoginRequestDTO loginRequest =
            new LoginRequestDTO(usuario.getEmail(), "Password#1");

        assertThrows(
            ForbiddenException.class,
            () -> service.login(loginRequest, "127.0.0.1")
        );
        
        verify(loginAttemptService).registerFailedLogin("127.0.0.1");
=======
    void loginEmailInexistente_registraIntentoFallido() {
        when(usuarioRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> service.login(
                new LoginRequestDTO("missing@test.com", "Password#1"), "127.0.0.1"));

        verify(loginAttemptService).registerFailedLogin("127.0.0.1");
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginConContrasenaIncorrecta_registraIntentoFallido() {
        Cliente usuario = cliente();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(AuthException.class, () -> service.login(
                new LoginRequestDTO(usuario.getEmail(), "wrong"), "127.0.0.1"));

        verify(loginAttemptService).registerFailedLogin("127.0.0.1");
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginDeUsuarioBloqueado_esProhibido() {
        Cliente usuario = cliente();
        usuario.setBloqueado(true);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", "hash")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> service.login(
                new LoginRequestDTO(usuario.getEmail(), "Password#1"), "127.0.0.1"));

        verify(loginAttemptService).registerFailedLogin("127.0.0.1");
        verify(authFactorService, never()).requiereMfaObligatorio(any());
    }

    @Test
    void loginConContrasenaExpirada_noAutentica() {
        Cliente usuario = cliente();
        usuario.setFechaCambioContrasena(LocalDateTime.now().minusSeconds(1));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", "hash")).thenReturn(true);

        assertThrows(PasswordExpiredException.class, () -> service.login(
                new LoginRequestDTO(usuario.getEmail(), "Password#1"), "127.0.0.1"));

        verify(loginAttemptService).registerFailedLogin("127.0.0.1");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginQueRequiereConfigurarMfa_devuelveChallengeSinTokens() {
        Cliente usuario = cliente();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", "hash")).thenReturn(true);
        when(authFactorService.requiereMfaObligatorio(usuario)).thenReturn(true);
        when(authFactorService.createSetupChallenge(usuario)).thenReturn("setup-token");

        LoginResponseDTO result = service.login(
                new LoginRequestDTO(usuario.getEmail(), "Password#1"), "127.0.0.1");

        assertEquals("REQUIRES_MFA_SETUP", result.mfaStatus());
        assertEquals("setup-token", result.mfaSetupToken());
        assertEquals(usuario.getEmail(), result.email());
        assertNull(result.accessToken());
        assertNull(result.refreshToken());
        verify(refreshTokenRepository, never()).save(any());
    }

    private Cliente cliente() {
        Cliente usuario = Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("hash")
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .tipoCliente(TipoCliente.NORMAL)
                .build();
        usuario.setId("user-1");
        usuario.setActivo(true);
        usuario.setFechaCambioContrasena(LocalDateTime.now().plusDays(1));
        return usuario;
>>>>>>> 139f0bfa67e3f22919e97df783e7ecffbe326765
    }
}
