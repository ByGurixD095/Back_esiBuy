package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordRecoveryTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private EmailService emailService;
    @Mock private AuthFactorService authFactorService;
    @Mock private BCryptPasswordEncoder encoder;
    @InjectMocks private AuthService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "passwordResetUrl", "https://example.test/reset?token=");
    }

    @Test
    void requestPasswordReset_guardaHashYEnvíaEnlaceConToken() throws Exception {
        Cliente usuario = cliente();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        service.requestPasswordReset(new PasswordResetRequestDTO(usuario.getEmail()));

        verify(usuarioRepository).save(usuario);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendRecoveryEmail(
                eq(usuario.getEmail()), eq(usuario.getNombre()), linkCaptor.capture());

        String link = linkCaptor.getValue();
        String token = link.substring(link.indexOf("token=") + "token=".length());
        assertFalse(token.isBlank());
        assertNotEquals(token, usuario.getTokenRecuperacionContrasena());
        assertEquals(hash(token), usuario.getTokenRecuperacionContrasena());
        assertTrue(usuario.getFechaExpiracionTokenRecuperacion().isAfter(LocalDateTime.now()));
        assertTrue(usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now().plusMinutes(6)));
    }

    @Test
    void requestPasswordReset_emailDesconocidoNoGuardaNiEnviaEmail() throws Exception {
        when(usuarioRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        service.requestPasswordReset(new PasswordResetRequestDTO("missing@test.com"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).sendRecoveryEmail(any(), any(), any());
    }

    @Test
    void resetPassword_cambiaContrasenaActualizaHistorialYConsumeToken() {
        Cliente usuario = cliente();
        String token = "valid-token";
        usuario.setTokenRecuperacionContrasena(hash(token));
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().plusMinutes(5));
        usuario.setHistorialContrasenas(new java.util.ArrayList<>());
        when(usuarioRepository.findByTokenRecuperacionContrasena(hash(token))).thenReturn(Optional.of(usuario));
        when(encoder.encode("NuevaClave1!")).thenReturn("encoded-new-password");

        service.resetPassword(new PasswordResetConfirmDTO(token, "NuevaClave1!", "NuevaClave1!"));

        verify(passwordValidatorService).validatePassword("NuevaClave1!", usuario.getHistorialContrasenas());
        assertEquals("encoded-new-password", usuario.getContrasena());
        assertEquals(java.util.List.of("encoded-new-password"), usuario.getHistorialContrasenas());
        assertTrue(usuario.getFechaCambioContrasena().isAfter(LocalDateTime.now()));
        assertNull(usuario.getTokenRecuperacionContrasena());
        assertNull(usuario.getFechaExpiracionTokenRecuperacion());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void resetPassword_contrasenasDiferentesNoConsultaToken() {
        assertThrows(ValidationException.class, () -> service.resetPassword(
                new PasswordResetConfirmDTO("token", "NuevaClave1!", "OtraClave1!")));

        verify(usuarioRepository, never()).findByTokenRecuperacionContrasena(any());
    }

    @Test
    void resetPassword_tokenExpiradoNoGuardaCambios() {
        Cliente usuario = cliente();
        String token = "expired-token";
        usuario.setTokenRecuperacionContrasena(hash(token));
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().minusSeconds(1));
        when(usuarioRepository.findByTokenRecuperacionContrasena(hash(token))).thenReturn(Optional.of(usuario));

        assertThrows(ValidationException.class, () -> service.resetPassword(
                new PasswordResetConfirmDTO(token, "NuevaClave1!", "NuevaClave1!")));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void resetPassword_tokenDesconocidoEsNotFound() {
        when(usuarioRepository.findByTokenRecuperacionContrasena(hash("missing"))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.resetPassword(
                new PasswordResetConfirmDTO("missing", "NuevaClave1!", "NuevaClave1!")));
    }

    private Cliente cliente() {
        return Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("old-hash")
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build();
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
