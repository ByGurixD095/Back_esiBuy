package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceEliminarCuentaTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private AuthService authService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                authService);
    }

    @Test
    void eliminarCuenta_clienteSeMarcaEliminadoInactivoYRevocaRefreshTokens() {
        Cliente cliente = new Cliente();
        cliente.setActivo(true);
        when(usuarioRepository.findById("cliente-1"))
                .thenReturn(Optional.of(cliente));

        userService.eliminarCuenta("cliente-1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        verify(authService).revocarRefreshTokens("cliente-1");
    }

    @Test
    void eliminarCuenta_noEliminaElUltimoAdministradorActivo() {
        Administrador administrador = new Administrador();
        administrador.setActivo(true);
        when(usuarioRepository.findById("admin-1"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndActivoTrueAndBloqueadoFalse(Rol.ADMINISTRADOR))
                .thenReturn(1L);

        assertThrows(ForbiddenException.class, () -> userService.eliminarCuenta("admin-1"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(authService, never()).revocarRefreshTokens("admin-1");
    }

    @Test
    void eliminarCuenta_permiteEliminarAdministradorSiQuedaOtroActivo() {
        Administrador administrador = new Administrador();
        administrador.setActivo(true);
        when(usuarioRepository.findById("admin-2"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndActivoTrueAndBloqueadoFalse(Rol.ADMINISTRADOR))
                .thenReturn(2L);

        userService.eliminarCuenta("admin-2");

        assertFalse(administrador.isActivo());
        verify(usuarioRepository).save(administrador);
        verify(authService).revocarRefreshTokens("admin-2");
    }

    @Test
    void eliminarCuenta_usuarioNoEncontradoLanzaNotFound() {
        when(usuarioRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.eliminarCuenta("no-existe"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(authService, never()).revocarRefreshTokens("no-existe");
    }
}
