package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.PasswordExpiredException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
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

    @Test
    void loginConMfaConfigurado_devuelveVerificacionYPreparaOtpCuandoEsVendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("seller-1");
        vendedor.setEmail("seller@test.com");
        vendedor.setContrasena("hash");
        vendedor.setRol(Rol.VENDEDOR);
        vendedor.setActivo(true);
        vendedor.setMfaConfigurado(true);
        vendedor.setFechaCambioContrasena(LocalDateTime.now().plusDays(1));
        when(usuarioRepository.findByEmail(vendedor.getEmail())).thenReturn(Optional.of(vendedor));
        when(encoder.matches("Password#1", "hash")).thenReturn(true);
        when(authFactorService.requiereMfaObligatorio(vendedor)).thenReturn(true);

        LoginResponseDTO result = service.login(
                new LoginRequestDTO(vendedor.getEmail(), "Password#1"), "127.0.0.1");

        assertEquals("REQUIRES_MFA_VERIFICATION", result.mfaStatus());
        assertEquals(vendedor.getEmail(), result.email());
        assertNull(result.accessToken());
        verify(authFactorService).prepareVerification(vendedor);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void completarAutenticacionParaClienteConMfaConfigurado_noPreparaOtpDeEmail() {
        Cliente usuario = cliente();
        usuario.setMfaConfigurado(true);
        when(authFactorService.requiereMfaObligatorio(usuario)).thenReturn(true);

        LoginResponseDTO result = service.completarAutenticacion(usuario);

        assertEquals("REQUIRES_MFA_VERIFICATION", result.mfaStatus());
        verify(authFactorService, never()).prepareVerification(usuario);
    }

    @Test
    void verifyMfa_validaFactoresYGeneraTokens() {
        Cliente usuario = cliente();
        MfaVerifyRequestDTO request = new MfaVerifyRequestDTO(usuario.getEmail(), "123456", null);
        when(authFactorService.verifyFactors(request)).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);

        LoginResponseDTO result = service.verifyMFA(request);

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertEquals("SUCCESS", result.mfaStatus());
        verify(authFactorService).verifyFactors(request);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void generarTokens_siFallaGuardarRefreshToken_lanzaErrorDeNegocio() {
        Cliente usuario = cliente();
        when(jwtService.generateToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);
        doThrow(new IllegalStateException("database unavailable"))
                .when(refreshTokenRepository).save(any());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.generarTokens(usuario));

        assertEquals("INTERNAL_ERROR", error.getErrorCode());
        verify(refreshTokenRepository).save(any());
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
    }
}
