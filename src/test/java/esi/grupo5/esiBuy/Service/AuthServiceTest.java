package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void loginSinMfa_obtieneTokens() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("user@test.com");
        usuario.setContrasena(new BCryptPasswordEncoder().encode("Password#1"));
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setBloqueado(false);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.matches("Password#1", usuario.getContrasena())).thenReturn(true);
        when(authFactorService.requiereMfaObligatorio(usuario)).thenReturn(false);
        when(jwtService.generateToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(3600);

        LoginResponseDTO result = service.login(
                new LoginRequestDTO(usuario.getEmail(), "Password#1"), "127.0.0.1");

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        verify(refreshTokenRepository).save(any());
    }

    @Test
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
    }
}
