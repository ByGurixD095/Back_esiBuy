package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceConsultarUsuariosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    @Mock
    private LoginAttemptService loginAttemptService;

    private UserService crearServicio() {
        return new UserService(
                usuarioRepository,
                jwtService,
                refreshTokenRepository,
                passwordValidatorService,
                loginAttemptService
        );
    }

    @Test
    void getAllUsers_devuelveUsuariosComoDto() {
        Cliente cliente = new Cliente();
        cliente.setId("cliente-1");
        cliente.setNombre("Ana");
        cliente.setApellidos("Pérez");
        cliente.setEmail("ana@test.com");
        cliente.setContrasena("hash-secreto");
        cliente.setActivo(true);
        cliente.setBloqueado(false);

        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        vendedor.setNombre("Luis");
        vendedor.setApellidos("Díaz");
        vendedor.setEmail("luis@test.com");
        vendedor.setActivo(false);
        vendedor.setBloqueado(true);

        when(usuarioRepository.findAll()).thenReturn(List.of(cliente, vendedor));

        List<UserDto> usuarios = crearServicio().getAllUsers();

        assertEquals(2, usuarios.size());
        assertEquals("cliente-1", usuarios.get(0).getId());
        assertEquals("Ana", usuarios.get(0).getName());
        assertEquals("Pérez", usuarios.get(0).getApellidos());
        assertEquals("ana@test.com", usuarios.get(0).getEmail());
        assertEquals(Rol.CLIENTE, usuarios.get(0).getRol());
        assertTrue(usuarios.get(0).isActivo());
        assertFalse(usuarios.get(0).isBloqueado());
        assertEquals("vendedor-1", usuarios.get(1).getId());
        assertEquals(Rol.VENDEDOR, usuarios.get(1).getRol());
        assertFalse(usuarios.get(1).isActivo());
        assertTrue(usuarios.get(1).isBloqueado());

        verify(usuarioRepository).findAll();
    }

    @Test
    void getAllUsers_sinUsuarios_devuelveListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), crearServicio().getAllUsers());
        verify(usuarioRepository).findAll();
    }

    @Test
    void getUserById_usuarioExistente_devuelveDto() {
        Cliente cliente = new Cliente();
        cliente.setId("cliente-1");
        cliente.setNombre("Ana");
        cliente.setApellidos("Pérez");
        cliente.setEmail("ana@test.com");
        cliente.setContrasena("hash-secreto");
        cliente.setActivo(true);
        cliente.setBloqueado(true);
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        UserDto usuario = crearServicio().getUserById("cliente-1");

        assertEquals("cliente-1", usuario.getId());
        assertEquals("Ana", usuario.getName());
        assertEquals("Pérez", usuario.getApellidos());
        assertEquals("ana@test.com", usuario.getEmail());
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertTrue(usuario.isActivo());
        assertTrue(usuario.isBloqueado());
        verify(usuarioRepository).findById("cliente-1");
    }

    @Test
    void getUserById_usuarioInexistente_lanza404() {
        when(usuarioRepository.findById("no-existe")).thenReturn(Optional.empty());
        UserService service = crearServicio();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.getUserById("no-existe")
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("Usuario no encontrado", error.getReason());
        verify(usuarioRepository).findById("no-existe");
    }
}
