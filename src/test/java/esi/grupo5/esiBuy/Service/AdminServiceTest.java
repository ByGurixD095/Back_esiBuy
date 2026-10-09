package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                List.<UsuarioUpdateStrategy>of());
    }

    @Test
    void crearAdministrador_guardaElRolYUnaContrasenaHasheada() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.crearAdministrador(dto);

        assertEquals(201, response.getStatusCode().value());
        AdministradorResponseDTO body = response.getBody();
        assertEquals("ana@esibuy.com", body.email());
        assertEquals("ADMINISTRADOR", body.rol());
        assertEquals("Administrador creado correctamente", body.mensaje());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertInstanceOf(Administrador.class, saved);
        assertEquals(Rol.ADMINISTRADOR, saved.getRol());
        assertTrue(new BCryptPasswordEncoder().matches("Clave#2026x", saved.getContrasena()));
    }

    @Test
    void crearAdministrador_emailExistenteLanzaConflictoYSinGuardar() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(new Administrador()));

        assertThrows(ConflictException.class, () -> service.crearAdministrador(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearAdministrador_contrasenaQueNoCumplePoliticaNoGuarda() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Contraseña débil"))
                .when(passwordValidatorService).validatePassword(dto.contrasena(), List.of());

        assertThrows(ValidationException.class, () -> service.crearAdministrador(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private AdministradorRegistroDTO administradorDTO() {
        return new AdministradorRegistroDTO(
                "Ana", "Pérez", "ana@esibuy.com", "Clave#2026x", "Madrid");
    }
}
