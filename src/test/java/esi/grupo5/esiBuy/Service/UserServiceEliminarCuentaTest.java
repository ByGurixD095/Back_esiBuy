package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.ForbiddenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.LineaPedido;
import esi.grupo5.esiBuy.Model.Pedido;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.EstadoPedido;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Repository.PedidoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceEliminarCuentaTest {

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
    void eliminarCuenta_clienteSeEliminaYRevocaRefreshTokens() {
        Cliente cliente = new Cliente();
        cliente.setActivo(true);
        when(usuarioRepository.findById("cliente-1"))
                .thenReturn(Optional.of(cliente));
        when(pedidoRepository.findAllByClienteId("cliente-1")).thenReturn(java.util.List.of());

        userService.eliminarCuenta("cliente-1");

        verify(usuarioRepository).delete(cliente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
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

        verify(usuarioRepository, never()).delete(any(Usuario.class));
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

        verify(usuarioRepository).delete(administrador);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(authService).revocarRefreshTokens("admin-2");
    }

    @Test
    void eliminarCuenta_usuarioNoEncontradoLanzaNotFound() {
        when(usuarioRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.eliminarCuenta("no-existe"));

        verify(usuarioRepository, never()).delete(any(Usuario.class));
        verify(authService, never()).revocarRefreshTokens("no-existe");
    }

    @Test
    void eliminarMiCuenta_noPermiteQueUnAdministradorSeElimine() {
        when(usuarioRepository.findById("admin-1"))
                .thenReturn(Optional.of(new Administrador()));

        assertThrows(ForbiddenException.class, () -> userService.eliminarMiCuenta("admin-1"));

        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    void eliminarCuenta_clienteDesvinculaPeroConservaSuPedido() {
        Cliente cliente = Cliente.builder().nombre("Ana").apellidos("García").build();
        Pedido pedido = new Pedido();
        pedido.setClienteId("cliente-1");
        pedido.getLineas().add(new LineaPedido(
                "producto-1", "Teclado", "TEC-1", null, "vendedor-1", 1_000, 1, 1_000));
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findAllByClienteId("cliente-1")).thenReturn(java.util.List.of(pedido));

        userService.eliminarCuenta("cliente-1");

        verify(pedidoRepository).save(pedido);
        org.junit.jupiter.api.Assertions.assertNull(pedido.getClienteId());
        org.junit.jupiter.api.Assertions.assertEquals("Ana García", pedido.getNombreCliente());
        verify(usuarioRepository).delete(cliente);
    }

    @Test
    void eliminarCuenta_vendedorConPedidosActivosNoSeElimina() {
        Vendedor vendedor = new Vendedor();
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));
        when(pedidoRepository.existsByLineas_IdVendedorAndEstadoIn(
                org.mockito.ArgumentMatchers.eq("vendedor-1"),
                org.mockito.ArgumentMatchers.anyCollection())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.eliminarCuenta("vendedor-1"));

        verify(usuarioRepository, never()).delete(any(Usuario.class));
        verify(productoRepository, never()).deleteByIdVendedor("vendedor-1");
    }

    @Test
    void eliminarCuenta_vendedorSinPedidosActivosBorraSusProductos() {
        Vendedor vendedor = new Vendedor();
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));
        when(pedidoRepository.findByEstado(EstadoPedido.RECIBIDO)).thenReturn(java.util.List.of());

        userService.eliminarCuenta("vendedor-1");

        verify(usuarioRepository).delete(vendedor);
        verify(productoRepository).deleteByIdVendedor("vendedor-1");
        verify(authService).revocarRefreshTokens("vendedor-1");
    }

    @Test
    void eliminarCuenta_vendedorConPedidoAntiguoNoSeElimina() {
        Vendedor vendedor = new Vendedor();
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.getLineas().add(new LineaPedido(
                "producto-1", "Teclado", "TEC-1", null, null, 1_000, 1, 1_000));
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));
        when(pedidoRepository.findByEstado(EstadoPedido.RECIBIDO)).thenReturn(java.util.List.of(pedido));
        when(productoRepository.existsByIdAndIdVendedor("producto-1", "vendedor-1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.eliminarCuenta("vendedor-1"));

        verify(usuarioRepository, never()).delete(any(Usuario.class));
        verify(productoRepository, never()).deleteByIdVendedor("vendedor-1");
    }
}
