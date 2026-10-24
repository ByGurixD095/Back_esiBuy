package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void eliminarUsuario_delegaEnLaReglaComunDeEliminacion() {
        Administrador objetivo = new Administrador();
        when(usuarioRepository.findById("admin-2")).thenReturn(Optional.of(objetivo));

        service.eliminarUsuario("admin-2", "admin-1");

        verify(userService).eliminarCuenta("admin-2");
    }

    @Test
    void eliminarUsuario_administradorIntentaEliminarse_lanzaForbidden() {
        assertThrows(ForbiddenException.class, () -> service.eliminarUsuario("admin-1", "admin-1"));

        verify(userService, never()).eliminarCuenta("admin-1");
    }
}