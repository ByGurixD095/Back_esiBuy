package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Auditoría defensiva de Broken Object Property Level Authorization (Mass Assignment):
 * Evalúa si el endpoint de actualización de perfil de usuario permite
 * que un cliente estándar se autoasigne privilegios o categorías restringidas (PREMIUM).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceMassAssignmentTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private AuthService authService;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProductoRepository productoRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                authService,
                pedidoRepository,
                productoRepository);
    }

    @Test
    @DisplayName("Auditoría Mass Assignment: Comprobar si un cliente NORMAL puede autoasignarse PREMIUM vía modificarMiPerfil")
    void comprobarSiClientePuedeAutoasignarsePremium() {
        Cliente cliente = new Cliente();
        cliente.setId("cliente-1");
        cliente.setTipoCliente(TipoCliente.NORMAL);

        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        UserSelfUpdateDTO payload = new UserSelfUpdateDTO(
                null, null, null, null,
                TipoCliente.PREMIUM, // Intento de auto-escalada a PREMIUM
                null, null, null
        );

        userService.modificarMiPerfil("cliente-1", payload);

        System.out.println(">>> AUDITORIA MASS ASSIGNMENT -> TipoCliente resultante: " + cliente.getTipoCliente());

        // La regla de seguridad esperada: Un cliente NO debería poder cambiar su propia categoría a PREMIUM.
        assertEquals(TipoCliente.NORMAL, cliente.getTipoCliente(),
                "FALLO DE SEGURIDAD (Mass Assignment): El cliente pudo autoasignarse PREMIUM sin intervención administrativa");
    }
}
