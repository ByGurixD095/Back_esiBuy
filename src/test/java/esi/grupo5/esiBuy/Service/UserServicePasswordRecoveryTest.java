package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicePasswordRecoveryTest {

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

    @Mock
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                usuarioRepository,
                jwtService,
                refreshTokenRepository,
                passwordValidatorService,
                loginAttemptService,
                emailService);
    }

    @Test
    void solicitaRecuperacionGuardaTokenHashYEnviaEmail() throws Exception {
        Cliente usuario = cliente();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        userService.requestPasswordReset(new PasswordResetRequestDTO(usuario.getEmail()));

        verify(usuarioRepository).save(usuario);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendRecoveryEmail(eq(usuario.getEmail()), eq(usuario.getNombre()), linkCaptor.capture());

        String link = linkCaptor.getValue();
        String token = link.substring(link.indexOf("token=") + "token=".length());
        assertFalse(token.isBlank());
        assertNotEquals(token, usuario.getTokenRecuperacionContrasena());
        assertEquals(64, usuario.getTokenRecuperacionContrasena().length());
        assertTrue(usuario.getFechaExpiracionTokenRecuperacion().isAfter(LocalDateTime.now()));
        assertTrue(usuario.getFechaExpiracionTokenRecuperacion().isBefore(LocalDateTime.now().plusMinutes(6)));
    }

    @Test
    void solicitarRecuperacionParaEmailDesconocidoNoRevelaNiEnviaEmail() throws Exception {
        when(usuarioRepository.findByEmail("desconocido@test.com")).thenReturn(Optional.empty());

        userService.requestPasswordReset(new PasswordResetRequestDTO("desconocido@test.com"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).sendRecoveryEmail(any(), any(), any());
    }

    @Test
    void restableceContrasenaValidaYConsumeToken() {
        Cliente usuario = cliente();
        String token = "token-valido";
        usuario.setTokenRecuperacionContrasena(hash(token));
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().plusMinutes(5));
        usuario.setHistorialContrasenas(new ArrayList<>());
        when(usuarioRepository.findByTokenRecuperacionContrasena(hash(token))).thenReturn(Optional.of(usuario));
        doNothing().when(passwordValidatorService).passwordIsWeak(any(), eq(usuario.getHistorialContrasenas()), any());

        userService.resetPassword(new PasswordResetConfirmDTO(token, "NuevaClave1!", "NuevaClave1!"));

        assertTrue(new BCryptPasswordEncoder().matches("NuevaClave1!", usuario.getContrasena()));
        assertEquals(1, usuario.getHistorialContrasenas().size());
        assertEquals(usuario.getContrasena(), usuario.getHistorialContrasenas().get(0));
        assertTrue(usuario.getTokenRecuperacionContrasena() == null);
        assertTrue(usuario.getFechaExpiracionTokenRecuperacion() == null);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rechazaRestablecimientoConContrasenasDiferentesSinConsultarToken() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.resetPassword(
                        new PasswordResetConfirmDTO("token", "NuevaClave1!", "OtraClave1!")));

        assertEquals(400, exception.getStatusCode().value());
        verify(usuarioRepository, never()).findByTokenRecuperacionContrasena(any());
    }

    @Test
    void rechazaTokenExpirado() {
        Cliente usuario = cliente();
        String token = "token-expirado";
        usuario.setFechaExpiracionTokenRecuperacion(LocalDateTime.now().minusSeconds(1));
        when(usuarioRepository.findByTokenRecuperacionContrasena(hash(token))).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.resetPassword(
                        new PasswordResetConfirmDTO(token, "NuevaClave1!", "NuevaClave1!")));

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("expirado"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private Cliente cliente() {
        return Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena("hash-anterior")
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
