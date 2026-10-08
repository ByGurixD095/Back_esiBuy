package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceEliminarUsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(usuarioRepository, encoder, passwordValidatorService, List.of());
    }

    @Test
    void eliminarUsuario_usuarioNormal_marcaEliminadoYGuarda() {
        Cliente cliente = new Cliente();
        cliente.setId("cliente-1");
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1"))
                .thenReturn(Optional.of(cliente));

        adminService.eliminarUsuario("cliente-1", "admin-1");

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioGuardado.capture());
        assertTrue(usuarioGuardado.getValue().isEliminado());
        verify(usuarioRepository).findByIdAndEliminadoFalse("cliente-1");
    }

    @Test
    void eliminarUsuario_cuentaPropia_lanza400SinConsultarRepositorio() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> adminService.eliminarUsuario("admin-1", "admin-1")
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("No puedes eliminar tu propia cuenta", error.getReason());
        verify(usuarioRepository, never()).findByIdAndEliminadoFalse("admin-1");
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eliminarUsuario_usuarioNoExistenteOLoEliminado_lanza404SinGuardar() {
        when(usuarioRepository.findByIdAndEliminadoFalse("no-existe"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> adminService.eliminarUsuario("no-existe", "admin-1")
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("Usuario no encontrado", error.getReason());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eliminarUsuario_ultimoAdministrador_lanza400SinEliminarlo() {
        Administrador administrador = new Administrador();
        administrador.setId("admin-2");
        when(usuarioRepository.findByIdAndEliminadoFalse("admin-2"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndEliminadoFalse(Rol.ADMINISTRADOR)).thenReturn(1L);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> adminService.eliminarUsuario("admin-2", "admin-1")
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("No se puede eliminar el último administrador", error.getReason());
        assertFalse(administrador.isEliminado());
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eliminarUsuario_administradorConOtroAdministrador_marcaEliminadoYGuarda() {
        Administrador administrador = new Administrador();
        administrador.setId("admin-2");
        when(usuarioRepository.findByIdAndEliminadoFalse("admin-2"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndEliminadoFalse(Rol.ADMINISTRADOR)).thenReturn(2L);

        adminService.eliminarUsuario("admin-2", "admin-1");

        assertTrue(administrador.isEliminado());
        verify(usuarioRepository).save(administrador);
    }
}
