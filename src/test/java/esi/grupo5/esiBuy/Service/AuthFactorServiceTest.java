package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFactorServiceTest {
    @Mock private EmailService emailService;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private AuthFactorService service;

    @Test
    void vendedor_requiereMfa() {
        Vendedor usuario = new Vendedor();
        usuario.setRol(Rol.VENDEDOR);
        assertTrue(service.requiereMfaObligatorio(usuario));
    }

    @Test
    void clienteSinFactores_noRequiereMfa() {
        Cliente usuario = new Cliente();
        usuario.setRol(Rol.CLIENTE);
        assertFalse(service.requiereMfaObligatorio(usuario));
    }

    @Test
    void setupChallenge_seGuardaHasheadoYSePuedeValidar() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("vendedor@test.com");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String token = service.createSetupChallenge(usuario);

        assertNotNull(token);
        assertNotEquals(token, usuario.getMfaSetupTokenHash());
        assertNotNull(usuario.getMfaSetupTokenExpiracion());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertDoesNotThrow(() -> service.requireSetupAccess(usuario.getEmail(), token));
    }

    @Test
    void setupChallenge_invalido_esRechazado() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("vendedor@test.com");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertThrows(RuntimeException.class,
                () -> service.requireSetupAccess(usuario.getEmail(), "invalid"));
    }
}
