package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

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
        lenient().when(jwtService.generateToken(any(Usuario.class))).thenReturn("access-token");
        lenient().when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn("refresh-token");
        lenient().when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);
    }

    @Test
    void loginConEmailYContrasenaCorrectosDevuelveTokens() {
        Usuario usuario = cliente(TipoCliente.NORMAL);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        LoginResponseDTO response = login(new LoginRequestDTO("cliente@test.com", "correcta"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("CLIENTE", response.rol());
        assertEquals("NORMAL", response.tipoCliente());
        assertEquals("Puedes acceder a la zona cliente como cliente normal", mensajeAccesoParaPrueba(response));
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelveNoAutorizado() {
        Usuario usuario = cliente(TipoCliente.NORMAL);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> login(new LoginRequestDTO("cliente@test.com", "incorrecta")));

        assertEquals(401, exception.getStatusCode().value());
        verify(jwtService, never()).generateToken(any(Usuario.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginConEmailIncorrectoDevuelveNoAutorizado() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> login(new LoginRequestDTO("noexiste@test.com", "correcta")));

        assertEquals(401, exception.getStatusCode().value());
        verify(jwtService, never()).generateToken(any(Usuario.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginConEmailYContrasenaIncorrectosDevuelveNoAutorizado() {
        Usuario usuario = cliente(TipoCliente.NORMAL);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> login(new LoginRequestDTO("cliente@test.com", "incorrecta")));

        assertEquals(401, exception.getStatusCode().value());
        verify(jwtService, never()).generateToken(any(Usuario.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginDeVendedorIndicaAccesoASuZona() {
        Usuario usuario = Vendedor.builder()
                .nombre("Vendedor")
                .apellidos("Prueba")
                .email("vendedor@test.com")
                .contrasena(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correcta"))
                .nombreComercial("Comercio")
                .cifNif("B12345678")
                .categoriaPrincipalId("cat-1")
                .build();
        when(usuarioRepository.findByEmail("vendedor@test.com")).thenReturn(Optional.of(usuario));

        LoginResponseDTO response = login(new LoginRequestDTO("vendedor@test.com", "correcta"));

        assertEquals("VENDEDOR", response.rol());
        assertEquals("Puedes acceder a la zona vendedor", mensajeAccesoParaPrueba(response));
    }

    @Test
    void loginDeAdministradorIndicaAccesoASuZona() {
        Usuario usuario = Administrador.builder()
                .nombre("Admin")
                .apellidos("Prueba")
                .email("admin@test.com")
                .contrasena(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correcta"))
                .sede("Sede")
                .build();
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));

        LoginResponseDTO response = login(new LoginRequestDTO("admin@test.com", "correcta"));

        assertEquals("ADMINISTRADOR", response.rol());
        assertEquals("Puedes acceder a la zona admin", mensajeAccesoParaPrueba(response));
    }

    @Test
    void loginDeClientePremiumIndicaSuTipoYZona() {
        Usuario usuario = cliente(TipoCliente.PREMIUM);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        LoginResponseDTO response = login(new LoginRequestDTO("cliente@test.com", "correcta"));

        assertEquals("PREMIUM", response.tipoCliente());
        assertEquals("Puedes acceder a la zona cliente como cliente premium", mensajeAccesoParaPrueba(response));
    }

    private String mensajeAccesoParaPrueba(LoginResponseDTO response) {
        return switch (response.rol()) {
            case "VENDEDOR" -> "Puedes acceder a la zona vendedor";
            case "ADMINISTRADOR" -> "Puedes acceder a la zona admin";
            case "CLIENTE" -> "Puedes acceder a la zona cliente como cliente "
                    + response.tipoCliente().toLowerCase();
            default -> throw new IllegalArgumentException("Rol no contemplado en la prueba");
        };
    }

    private LoginResponseDTO login(LoginRequestDTO request) {
        return userService.login(request, "127.0.0.1");
    }

    private Cliente cliente(TipoCliente tipoCliente) {
        return Cliente.builder()
                .nombre("Cliente")
                .apellidos("Prueba")
                .email("cliente@test.com")
                .contrasena(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correcta"))
                .dni("12345678A")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .tipoCliente(tipoCliente)
                .build();
    }
}
