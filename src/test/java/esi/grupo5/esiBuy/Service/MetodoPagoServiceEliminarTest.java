package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceEliminarTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private MetodoPagoService metodoPagoService;

    @BeforeEach
    void setUp() {
        metodoPagoService = new MetodoPagoService(
                metodoPagoRepository,
                usuarioRepository);
    }

    @Test
    void eliminarMetodoPago_metodoPropio_existenteSeElimina() {
        String clienteId = "cliente123";
        String metodoPagoId = "pago123";

        Cliente cliente = mock(Cliente.class);

        MetodoPago metodoPago = new MetodoPago(
                clienteId,
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234");
        metodoPago.setId(metodoPagoId);

        when(usuarioRepository.findById(clienteId))
                .thenReturn(Optional.<Usuario>of(cliente));

        when(metodoPagoRepository.findByIdAndClienteId(
                metodoPagoId, clienteId))
                .thenReturn(Optional.of(metodoPago));

        metodoPagoService.eliminarMetodoPago(clienteId, metodoPagoId);

        verify(metodoPagoRepository).delete(metodoPago);
    }

    @Test
    void eliminarMetodoPago_clienteNoExiste_lanzaNotFound() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.eliminarMetodoPago(
                        "cliente123", "pago123"));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void eliminarMetodoPago_usuarioNoEsCliente_lanzaValidation() {
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findById("usuario123"))
                .thenReturn(Optional.of(usuario));

        assertThrows(ValidationException.class,
                () -> metodoPagoService.eliminarMetodoPago(
                        "usuario123", "pago123"));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void eliminarMetodoPago_metodoNoExiste_lanzaNotFound() {
        Cliente cliente = mock(Cliente.class);

        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.<Usuario>of(cliente));

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", "cliente123"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.eliminarMetodoPago(
                        "cliente123", "pago123"));

        verify(metodoPagoRepository, never()).delete(any(MetodoPago.class));
    }

    @Test
    void eliminarMetodoPago_metodoDeOtroCliente_noSeElimina() {
        Cliente cliente = mock(Cliente.class);

        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.<Usuario>of(cliente));

        // La consulta conjunta no encuentra métodos ajenos.
        when(metodoPagoRepository.findByIdAndClienteId(
                "pagoAjeno", "cliente123"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.eliminarMetodoPago(
                        "cliente123", "pagoAjeno"));

        verify(metodoPagoRepository, never()).delete(any(MetodoPago.class));
    }

    @Test
    void eliminarMetodoPago_errorAlBorrar_lanzaBusinessException() {
        String clienteId = "cliente123";
        String metodoPagoId = "pago123";

        Cliente cliente = mock(Cliente.class);

        MetodoPago metodoPago = new MetodoPago(
                clienteId,
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234");
        metodoPago.setId(metodoPagoId);

        when(usuarioRepository.findById(clienteId))
                .thenReturn(Optional.<Usuario>of(cliente));

        when(metodoPagoRepository.findByIdAndClienteId(
                metodoPagoId, clienteId))
                .thenReturn(Optional.of(metodoPago));

        doThrow(new DataAccessResourceFailureException("Error de prueba"))
                .when(metodoPagoRepository).delete(metodoPago);

        assertThrows(BusinessException.class,
                () -> metodoPagoService.eliminarMetodoPago(
                        clienteId, metodoPagoId));

        verify(metodoPagoRepository).delete(metodoPago);
    }
}