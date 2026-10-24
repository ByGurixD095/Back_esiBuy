
package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Dto.MetodoPagoRequestDTO;
import esi.grupo5.esiBuy.Dto.MetodoPagoResponseDTO;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.MetodoPago;
import esi.grupo5.esiBuy.Model.enums.TipoMetodoPago;
import esi.grupo5.esiBuy.Repository.MetodoPagoRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MetodoPagoService metodoPagoService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = mock(Cliente.class);
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(cliente));
    }

    @Test
    void registrarTarjetaValida_GuardaReferenciaEnmascarada() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "4111111111111234",
                "Ana Pérez",
                "12/2028",
                null,
                null
        );

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> {
                    MetodoPago guardado = invocation.getArgument(0);
                    guardado.setId("pago123");
                    return guardado;
                });

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.registrarMetodoPago("cliente123", dto);

        assertEquals("pago123", respuesta.id());
        assertEquals(TipoMetodoPago.TARJETA, respuesta.tipo());
        assertEquals("Mi tarjeta", respuesta.alias());
        assertEquals("**** 1234", respuesta.referencia());
        assertEquals("Ana Pérez", respuesta.titular());
        assertEquals("12/2028", respuesta.caducidad());

        verify(metodoPagoRepository).save(any(MetodoPago.class));
    }

    @Test
    void registrarPaypalValido_GuardaCorreoComoReferencia() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                "cliente@example.com",
                null
        );

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.registrarMetodoPago("cliente123", dto);

        assertEquals(TipoMetodoPago.PAYPAL, respuesta.tipo());
        assertEquals("cliente@example.com", respuesta.referencia());
    }

    @Test
    void registrarBizumValido_GuardaTelefonoComoReferencia() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.BIZUM,
                "Mi Bizum",
                null,
                null,
                null,
                null,
                "612345678"
        );

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetodoPagoResponseDTO respuesta =
                metodoPagoService.registrarMetodoPago("cliente123", dto);

        assertEquals(TipoMetodoPago.BIZUM, respuesta.tipo());
        assertEquals("612345678", respuesta.referencia());
    }

    @Test
    void registrarTarjetaSinNumero_LanzaExcepcion() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                null,
                "Ana Pérez",
                "12/2028",
                null,
                null
        );

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago("cliente123", dto));

        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    void registrarTarjetaSinTitular_LanzaExcepcion() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "4111111111111234",
                " ",
                "12/2028",
                null,
                null
        );

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago("cliente123", dto));

        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    void registrarTarjetaSinCaducidad_LanzaExcepcion() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "4111111111111234",
                "Ana Pérez",
                null,
                null,
                null
        );

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago("cliente123", dto));

        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    void registrarPaypalSinCorreo_LanzaExcepcion() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.PAYPAL,
                "Mi PayPal",
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago("cliente123", dto));

        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    void registrarBizumSinTelefono_LanzaExcepcion() {
        MetodoPagoRequestDTO dto = new MetodoPagoRequestDTO(
                TipoMetodoPago.BIZUM,
                "Mi Bizum",
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago("cliente123", dto));

        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }
}
