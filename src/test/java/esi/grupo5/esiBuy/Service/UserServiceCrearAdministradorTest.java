package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class AdminServiceCrearAdministradorTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordValidatorService passwordValidatorService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private AdminService adminService;

    private final AdministradorRegistroDTO dto = new AdministradorRegistroDTO("Ana", "Pérez", "ana@esibuy.com",
            "Clave#2026x", "Madrid");

    @BeforeEach
    void setUp() {
        adminService = new AdminService(usuarioRepository, encoder, passwordValidatorService, List.of());
    }

    @Test
    void crearAdministrador_guardaAdminConRolYContrasenaHasheada() {
        when(usuarioRepository.findByEmail("ana@esibuy.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<AdministradorResponseDTO> respuesta = adminService.crearAdministrador(dto);

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
        assertTrue(encoder.matches("Clave#2026x", guardado.getContrasena()));
    }

        @Test
    void crearAdministrador_emailYaRegistrado_lanzaErrorYNoGuarda() {
        when(usuarioRepository.findByEmail("ana@esibuy.com")).thenReturn(Optional.of(new Administrador()));

        BusinessException error = assertThrows(ConflictException.class,
                () -> adminService.crearAdministrador(dto));

        assertEquals(409, error.getHttpStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearAdministrador_contrasenaNoValida_lanza400YNoGuarda() {
        when(usuarioRepository.findByEmail("ana@esibuy.com")).thenReturn(Optional.empty());
        doThrow(new ValidationException("demasiado débil"))
                .when(passwordValidatorService).validatePassword(anyString(), anyList());

        BusinessException error = assertThrows(ValidationException.class,
                () -> adminService.crearAdministrador(dto));

        assertEquals(400, error.getHttpStatusCode());
        verify(usuarioRepository, never()).save(any());
    }
}