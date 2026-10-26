package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceNombreComercialTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    @Mock
    private AuthService authService;

    private UserService crearServicio() {
        return new UserService(usuarioRepository, passwordEncoder, passwordValidatorService, authService);
    }

    @Test
    void getNombreComercialById_devuelveNombreDelVendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setNombreComercial("Tienda ESI");
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));

        String nombreComercial = crearServicio().getNombreComercialById("vendedor-1");

        assertEquals("Tienda ESI", nombreComercial);
        verify(usuarioRepository).findById("vendedor-1");
    }

    @Test
    void getNombreComercialById_lanzaNotFoundSiElVendedorNoExiste() {
        when(usuarioRepository.findById("vendedor-inexistente")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> crearServicio().getNombreComercialById("vendedor-inexistente"));

        verify(usuarioRepository).findById("vendedor-inexistente");
    }

    @Test
    void getNombreComercialById_lanzaNotFoundSiElUsuarioNoEsVendedor() {
        Usuario cliente = new Cliente();
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        assertThrows(NotFoundException.class,
                () -> crearServicio().getNombreComercialById("cliente-1"));

        verify(usuarioRepository).findById("cliente-1");
    }
}
