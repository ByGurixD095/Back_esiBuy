package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Dto.DireccionEnvioDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.DireccionGuardada;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class DireccionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DireccionService direccionService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setDirecciones(new ArrayList<>());
    }

    @Test
    void crearGuardaDatosNormalizadosEnLaCuentaDelCliente() {
        prepararCliente();
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var direccion = direccionService.crear("cliente-1", dto(" Ana García "));

        assertEquals("Ana García", direccion.nombreCompleto());
        assertEquals(1, cliente.getDirecciones().size());
        verify(usuarioRepository).save(cliente);
    }

    @Test
    void actualizarSoloModificaLaDireccionPropia() {
        prepararCliente();
        DireccionGuardada guardada = new DireccionGuardada(
                "Ana", "Calle 1", "Madrid", "Madrid", "28001", "España", "600000000");
        guardada.setId("direccion-1");
        cliente.getDirecciones().add(guardada);
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var actualizada = direccionService.actualizar("cliente-1", "direccion-1", dto("Ana García"));

        assertEquals("Ana García", actualizada.nombreCompleto());
        assertEquals("Ana García", cliente.getDirecciones().get(0).getNombreCompleto());
    }

    @Test
    void actualizarDireccionAjenaDevuelveNoEncontrado() {
        prepararCliente();
        assertThrows(NotFoundException.class,
                () -> direccionService.actualizar("cliente-1", "ajena", dto("Ana")));
    }

    @Test
    void noClienteNoPuedeGestionarDirecciones() {
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(new Vendedor()));

        assertThrows(ValidationException.class, () -> direccionService.listar("vendedor-1"));
    }

    private DireccionEnvioDTO dto(String nombre) {
        return new DireccionEnvioDTO(
                nombre, " Calle Mayor 1 ", " Madrid ", " Madrid ",
                " 28001 ", " España ", " +34600000000 ");
    }

    private void prepararCliente() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
    }
}
