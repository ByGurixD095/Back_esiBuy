package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceEliminarUsuarioTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private UserService userService;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                List.<UsuarioUpdateStrategy>of(), userService);
    }

    @Test
    void eliminarUsuario_cuentaAjenaNoAdministradora_seMarcaComoEliminada() {
        Cliente cliente = new Cliente();
        cliente.setId("cliente-1");
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1"))
                .thenReturn(Optional.of(cliente));

        service.eliminarUsuario("cliente-1", "admin-1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertTrue(captor.getValue().isEliminado());
    }

    @Test
    void eliminarUsuario_administradorIntentaEliminarse_lanzaForbidden() {
        assertThrows(ForbiddenException.class, () -> service.eliminarUsuario("admin-1", "admin-1"));

        verify(usuarioRepository, never()).findByIdAndEliminadoFalse(any());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_idNoEncontrado_lanzaNotFound() {
        when(usuarioRepository.findByIdAndEliminadoFalse("no-existe")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.eliminarUsuario("no-existe", "admin-1"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_ultimoAdministrador_lanzaForbiddenYSigueActivo() {
        Administrador administrador = new Administrador();
        when(usuarioRepository.findByIdAndEliminadoFalse("admin-2"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndEliminadoFalse(Rol.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> service.eliminarUsuario("admin-2", "admin-1"));

        assertFalse(administrador.isEliminado());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_administradorConOtrosAdministradores_seMarcaComoEliminado() {
        Administrador administrador = new Administrador();
        when(usuarioRepository.findByIdAndEliminadoFalse("admin-2"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndEliminadoFalse(Rol.ADMINISTRADOR)).thenReturn(2L);

        service.eliminarUsuario("admin-2", "admin-1");

        assertTrue(administrador.isEliminado());
        verify(usuarioRepository).save(administrador);
    }
}
