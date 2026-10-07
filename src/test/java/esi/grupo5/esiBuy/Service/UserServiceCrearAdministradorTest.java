package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceCrearAdministradorTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private EmailService emailService;
    @Mock private AuthFactorService authFactorService;

    @InjectMocks private UserService userService;

    private final AdministradorRegistroDTO dto =
            new AdministradorRegistroDTO("Ana", "Pérez", "ana@esibuy.com", "Clave#2026x", "Madrid");

    @Test
    void crearAdministrador_guardaAdminConRolYContrasenaHasheada() {
        when(usuarioRepository.findByEmail("ana@esibuy.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<AdministradorResponseDTO> respuesta = userService.crearAdministrador(dto);

        assertEquals(201, respuesta.getStatusCode().value());
        assertEquals("ana@esibuy.com", respuesta.getBody().email());
        assertEquals("ADMINISTRADOR", respuesta.getBody().rol());
        assertEquals("Administrador creado correctamente", respuesta.getBody().mensaje());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertInstanceOf(Administrador.class, guardado);
        assertEquals(Rol.ADMINISTRADOR, guardado.getRol());
        assertNotEquals("Clave#2026x", guardado.getContrasena());
        assertTrue(new BCryptPasswordEncoder().matches("Clave#2026x", guardado.getContrasena()));
    }

    @Test
    void crearAdministrador_emailYaRegistrado_lanzaErrorYNoGuarda() {
        when(usuarioRepository.findByEmail("ana@esibuy.com")).thenReturn(Optional.of(new Administrador()));

        assertThrows(ResponseStatusException.class, () -> userService.crearAdministrador(dto));

        verify(usuarioRepository, never()).save(any());
    }
}
