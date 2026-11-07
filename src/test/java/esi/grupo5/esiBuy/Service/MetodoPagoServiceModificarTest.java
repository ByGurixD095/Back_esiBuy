
package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.MetodoPagoRequestDTO;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceModificarTest {

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

    private MetodoPago crearMetodoPago(String clienteId) {
        MetodoPago metodo = new MetodoPago(
                clienteId,
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234");
        metodo.setId("pago123");
        metodo.setTitular("Ana Pérez");
        metodo.setCaducidad("12/2028");
        return metodo;
    }

    private void prepararCliente(String clienteId) {
        Cliente cliente = mock(Cliente.class);

        when(usuarioRepository.findById(clienteId))
                .thenReturn(Optional.<Usuario>of(cliente));
    }

    @Test
    void modificarMetodoPago_tarjeta_existenteSeModifica() {
        String clienteId = "cliente123";
        MetodoPago metodo = crearMetodoPago(clienteId);

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Nueva tarjeta",
                "1234567812345678",
                "Ana Pérez",
                "10/2029",
                null,
                null);

        prepararCliente(clienteId);

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", clienteId))
                .thenReturn(Optional.of(metodo));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto);

        assertEquals("pago123", respuesta.id());
        assertEquals(TipoMetodoPago.TARJETA, respuesta.tipo());
        assertEquals("Nueva tarjeta", respuesta.alias());
        assertEquals("**** 5678", respuesta.referencia());
        assertEquals("Ana Pérez", respuesta.titular());
        assertEquals("10/2029", respuesta.caducidad());

        verify(metodoPagoRepository).save(metodo);
    }

    @Test
    void modificarMetodoPago_paypal_existenteSeModifica() {
        String clienteId = "cliente123";
        MetodoPago metodo = crearMetodoPago(clienteId);

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        prepararCliente(clienteId);

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", clienteId))
                .thenReturn(Optional.of(metodo));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto);

        assertEquals(TipoMetodoPago.PAYPAL, respuesta.tipo());
        assertEquals("Mi PayPal", respuesta.alias());
        assertEquals("nuevo@gmail.com", respuesta.referencia());
        assertNull(respuesta.titular());
        assertNull(respuesta.caducidad());

        verify(metodoPagoRepository).save(metodo);
    }

    @Test
    void modificarMetodoPago_bizum_existenteSeModifica() {
        String clienteId = "cliente123";
        MetodoPago metodo = crearMetodoPago(clienteId);

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.BIZUM,
                "Mi Bizum",
                null,
                null,
                null,
                null,
                "612345678");

        prepararCliente(clienteId);

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", clienteId))
                .thenReturn(Optional.of(metodo));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto);

        assertEquals(TipoMetodoPago.BIZUM, respuesta.tipo());
        assertEquals("Mi Bizum", respuesta.alias());
        assertEquals("612345678", respuesta.referencia());
        assertNull(respuesta.titular());
        assertNull(respuesta.caducidad());

        verify(metodoPagoRepository).save(metodo);
    }

    @Test
    void modificarMetodoPago_aliasNulo_UtilizaTipoComoAlias() {
        String clienteId = "cliente123";
        MetodoPago metodo = crearMetodoPago(clienteId);

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                null,
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        prepararCliente(clienteId);

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", clienteId))
                .thenReturn(Optional.of(metodo));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto);

        assertEquals("PAYPAL", respuesta.alias());
    }

    @Test
    void modificarMetodoPago_clienteNoExiste_lanzaNotFound() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.empty());

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        "cliente123", "pago123", dto));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void modificarMetodoPago_usuarioNoEsCliente_lanzaValidation() {
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findById("usuario123"))
                .thenReturn(Optional.of(usuario));

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        assertThrows(ValidationException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        "usuario123", "pago123", dto));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void modificarMetodoPago_metodoNoExiste_lanzaNotFound() {
        prepararCliente("cliente123");

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", "cliente123"))
                .thenReturn(Optional.empty());

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        "cliente123", "pago123", dto));

        verify(metodoPagoRepository, never()).save(any());
    }

    @Test
    void modificarMetodoPago_metodoDeOtroCliente_noSeModifica() {
        prepararCliente("cliente123");

        when(metodoPagoRepository.findByIdAndClienteId(
                "pagoAjeno", "cliente123"))
                .thenReturn(Optional.empty());

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        "cliente123", "pagoAjeno", dto));

        verify(metodoPagoRepository, never()).save(any());
    }

    @Test
    void modificarMetodoPago_datosInvalidos_lanzaValidation() {
        String clienteId = "cliente123";
        prepararCliente(clienteId);

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                null,
                "Ana Pérez",
                "12/2028",
                null,
                null);

        assertThrows(ValidationException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto));

        verify(metodoPagoRepository, never()).save(any());
    }

    @Test
    void modificarMetodoPago_errorAlGuardar_lanzaBusinessException() {
        String clienteId = "cliente123";
        MetodoPago metodo = crearMetodoPago(clienteId);

        prepararCliente(clienteId);

        when(metodoPagoRepository.findByIdAndClienteId(
                "pago123", clienteId))
                .thenReturn(Optional.of(metodo));

        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "nuevo@gmail.com",
                null);

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenThrow(new DataAccessResourceFailureException(
                        "Error de prueba"));

        assertThrows(BusinessException.class,
                () -> metodoPagoService.modificarMetodoPago(
                        clienteId, "pago123", dto));

        verify(metodoPagoRepository).save(metodo);
    }
}
