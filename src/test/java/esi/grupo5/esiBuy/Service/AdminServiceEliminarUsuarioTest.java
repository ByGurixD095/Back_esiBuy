package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
        service.eliminarUsuario("cliente-1", "admin-1");

        verify(userService).eliminarCuenta("cliente-1");
    }

    @Test
    void eliminarUsuario_administradorIntentaEliminarse_lanzaForbidden() {
        assertThrows(ForbiddenException.class, () -> service.eliminarUsuario("admin-1", "admin-1"));

        verify(userService, never()).eliminarCuenta("admin-1");
    }
}