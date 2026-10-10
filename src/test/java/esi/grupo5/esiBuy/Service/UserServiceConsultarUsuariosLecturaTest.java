package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteResponseDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorResponseDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceConsultarUsuariosLecturaTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private AuthService authService;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                authService);
    }

    @Test
    void consultarUsuarios_existentesDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        cliente.setBloqueado(false);
        cliente.setFechaAlta(LocalDateTime.of(2025, 1, 2, 3, 4));
        cliente.setMfaConfigurado(true);
        cliente.setIs2faActivoCliente(true);
        cliente.setIs3faActivoCliente(true);
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of(cliente));

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        assertEquals(1, resultado.size());
        assertEquals("cliente-1", resultado.get(0).getId());
        assertEquals("Ana", resultado.get(0).getName());
        assertEquals("López", resultado.get(0).getApellidos());
        assertEquals("ana@test.com", resultado.get(0).getEmail());
        assertEquals(Rol.CLIENTE, resultado.get(0).getRol());
        assertEquals(true, resultado.get(0).isActivo());
        assertEquals(false, resultado.get(0).isBloqueado());
        assertEquals("12345678A", ((ClienteResponseDTO) resultado.get(0)).getDni());
        assertEquals(cliente.getFechaAlta(), resultado.get(0).getFechaAlta());
        assertEquals(cliente.getFechaCambioContrasena(), resultado.get(0).getFechaCambioContrasena());
        assertEquals(true, resultado.get(0).isMfaConfigurado());
        assertEquals(true, resultado.get(0).isDosFactorActivoCliente());
        assertEquals(true, resultado.get(0).isTresFactorActivoCliente());
        verify(usuarioRepository).findAllByEliminadoFalse();
    }

    @Test
    void consultarUsuarios_vendedorIncluyeSusDatosEspecificos() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        vendedor.setNombre("Luis");
        vendedor.setNombreComercial("Tienda Luis");
        vendedor.setCifNif("B12345678");
        vendedor.setCategoriaPrincipalId("electronica");
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of(vendedor));

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        VendedorResponseDTO dto = (VendedorResponseDTO) resultado.get(0);
        assertEquals("Tienda Luis", dto.getNombreComercial());
        assertEquals("B12345678", dto.getCifNif());
        assertEquals("electronica", dto.getCategoriaPrincipalId());
    }

    @Test
    void consultarUsuarios_administradorIncluyeDatosComunesYEspecificos() {
        Administrador administrador = Administrador.builder()
                .nombre("Marta")
                .apellidos("García")
                .email("marta@test.com")
                .contrasena("no-debe-exponerse")
                .sede("Madrid")
                .build();
        administrador.setId("admin-1");
        administrador.setMfaConfigurado(true);
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of(administrador));

        AdministradorResponseDTO resultado =
                (AdministradorResponseDTO) service.getAllUsers().get(0);

        assertEquals("admin-1", resultado.getId());
        assertEquals(Rol.ADMINISTRADOR, resultado.getRol());
        assertEquals("Madrid", resultado.getSede());
        assertEquals(administrador.getFechaIncorporacion(), resultado.getFechaIncorporacion());
        assertEquals(true, resultado.isMfaConfigurado());
        assertEquals(true, resultado.isActivo());
    }

    @Test
    void consultarUsuarios_sinUsuariosDevuelveListaVacia() {
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of());

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        assertEquals(List.of(), resultado);
        verify(usuarioRepository).findAllByEliminadoFalse();
    }

    @Test
    void consultarUsuarioPorId_existenteDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1"))
                .thenReturn(Optional.of(cliente));

        UsuarioResponseDTO resultado = service.getUserById("cliente-1");

        assertEquals("cliente-1", resultado.getId());
        assertEquals("Ana", resultado.getName());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals(true, resultado.isActivo());
        assertEquals(TipoCliente.NORMAL, ((ClienteResponseDTO) resultado).getTipoCliente());
        verify(usuarioRepository).findByIdAndEliminadoFalse("cliente-1");
    }

    @Test
    void consultarUsuarioPorId_inexistenteOLoEliminadoLanzaNotFound() {
        when(usuarioRepository.findByIdAndEliminadoFalse("no-existe"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUserById("no-existe"));

        verify(usuarioRepository).findByIdAndEliminadoFalse("no-existe");
    }

    private Cliente cliente(String id, String nombre, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellidos("López");
        cliente.setEmail(email);
        cliente.setRol(Rol.CLIENTE);
        cliente.setDni("12345678A");
        return cliente;
    }
}
