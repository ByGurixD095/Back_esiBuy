package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

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
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of(cliente));

        List<UserDto> resultado = service.getAllUsers();

        assertEquals(1, resultado.size());
        assertEquals("cliente-1", resultado.get(0).getId());
        assertEquals("Ana", resultado.get(0).getName());
        assertEquals("López", resultado.get(0).getApellidos());
        assertEquals("ana@test.com", resultado.get(0).getEmail());
        assertEquals(Rol.CLIENTE, resultado.get(0).getRol());
        assertEquals(true, resultado.get(0).isActivo());
        assertEquals(false, resultado.get(0).isBloqueado());
        verify(usuarioRepository).findAllByEliminadoFalse();
    }

    @Test
    void consultarUsuarios_sinUsuariosDevuelveListaVacia() {
        when(usuarioRepository.findAllByEliminadoFalse()).thenReturn(List.of());

        List<UserDto> resultado = service.getAllUsers();

        assertEquals(List.of(), resultado);
        verify(usuarioRepository).findAllByEliminadoFalse();
    }

    @Test
    void consultarUsuarioPorId_existenteDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1"))
                .thenReturn(Optional.of(cliente));

        UserDto resultado = service.getUserById("cliente-1");

        assertEquals("cliente-1", resultado.getId());
        assertEquals("Ana", resultado.getName());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals(true, resultado.isActivo());
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
        return cliente;
    }
}
