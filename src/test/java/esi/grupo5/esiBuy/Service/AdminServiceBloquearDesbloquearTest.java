package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.UsuarioUpdateStrategy;
import esi.grupo5.esiBuy.Exception.NotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceBloquearDesbloquearTest {

    private final String id = "123pruebatest";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private BCryptPasswordEncoder encoder;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private List<UsuarioUpdateStrategy> updateStrategies;
    @Mock private UserService userService;

    @InjectMocks private AdminService adminService;

    @Test
    void bloquearUsuario_existe() {
        Vendedor usuario = new Vendedor();
        usuario.setBloqueado(false);

        UserDto dto = mock(UserDto.class);
        when(usuarioRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(usuario));
        when(userService.toDto(usuario)).thenReturn(dto);

        UserDto result = adminService.bloquearUsuario(id);

        assertTrue(usuario.isBloqueado());
        assertSame(dto, result);

        verify(usuarioRepository).save(usuario);
        verify(userService).toDto(usuario);
    }

    @Test
    void desbloquearUsuario_existe() {
        Vendedor usuario = new Vendedor();
        usuario.setBloqueado(true);

        UserDto dto = mock(UserDto.class);
        when(usuarioRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(usuario));
        when(userService.toDto(usuario)).thenReturn(dto);

        UserDto result = adminService.desbloquearUsuario(id);

        assertFalse(usuario.isBloqueado());
        assertSame(dto, result);

        verify(usuarioRepository).save(usuario);
        verify(userService).toDto(usuario);
    }

    @Test
    void bloquearUsuario_noExiste() {
        when(usuarioRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.bloquearUsuario(id));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desbloquearUsuario_noExiste() {
        when(usuarioRepository.findByIdAndEliminadoFalse(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.desbloquearUsuario(id));

        verify(usuarioRepository, never()).save(any());
    }
}
