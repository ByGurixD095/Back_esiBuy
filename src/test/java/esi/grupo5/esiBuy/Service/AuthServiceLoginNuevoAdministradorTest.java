package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginNuevoAdministradorTest {

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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService authService;

    private static final String EMAIL = "ana@esibuy.com";
    private static final String CONTRASENA = "Clave#2026x";

    @BeforeEach
    void crearAdministradorYDejarloGuardado() {
        AdminService adminService = new AdminService(usuarioRepository, encoder, passwordValidatorService, List.of());
        authService = new AuthService(usuarioRepository, jwtService, refreshTokenRepository,
                passwordValidatorService, loginAttemptService, emailService, encoder);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.crearAdministrador(
                new AdministradorRegistroDTO("Ana", "Pérez", EMAIL, CONTRASENA, "Madrid"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        // A partir de aquí, el repositorio "tiene" el administrador recién creado
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(captor.getValue()));
    }

    @Test
    void nuevoAdministrador_puedeIniciarSesionConSusCredenciales() {
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn("refresh-token");

        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO(EMAIL, CONTRASENA), "127.0.0.1");

        assertEquals("ADMINISTRADOR", respuesta.rol());
        assertEquals("access-token", respuesta.accessToken());
        assertEquals("refresh-token", respuesta.refreshToken());
    }

    @Test
    void nuevoAdministrador_conContrasenaIncorrecta_noPuedeIniciarSesion() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequestDTO(EMAIL, "OtraClave#1"), "127.0.0.1"));

        assertEquals(401, error.getStatusCode().value());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }
}