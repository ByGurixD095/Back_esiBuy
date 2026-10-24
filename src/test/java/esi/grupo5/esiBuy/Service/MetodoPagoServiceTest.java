
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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MetodoPagoService metodoPagoService;

    private Cliente cliente;
    private MetodoPagoRequestDTO request;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId("cliente123");

        request = new MetodoPagoRequestDTO(
                TipoMetodoPago.TARJETA,
                "Mi tarjeta",
                "**** 1234");
    }

    @Test
    void registrarMetodoPago_ClienteValido_GuardaMetodo() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(cliente));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenAnswer(invocation -> {
                    MetodoPago metodo = invocation.getArgument(0);
                    metodo.setId("pago123");
                    return metodo;
                });

        MetodoPagoResponseDTO resultado =
                metodoPagoService.registrarMetodoPago(
                        "cliente123", request);

        assertEquals("pago123", resultado.id());
        assertEquals(TipoMetodoPago.TARJETA, resultado.tipo());
        assertEquals("Mi tarjeta", resultado.alias());
        assertEquals("**** 1234", resultado.referencia());

        verify(metodoPagoRepository).save(any(MetodoPago.class));
    }

    @Test
    void registrarMetodoPago_ClienteInexistente_LanzaExcepcion() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> metodoPagoService.registrarMetodoPago(
                        "cliente123", request));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void registrarMetodoPago_UsuarioNoCliente_LanzaExcepcion() {
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(usuario));

        assertThrows(ValidationException.class,
                () -> metodoPagoService.registrarMetodoPago(
                        "cliente123", request));

        verifyNoInteractions(metodoPagoRepository);
    }

    @Test
    void registrarMetodoPago_ErrorMongoDB_LanzaBusinessException() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(cliente));

        when(metodoPagoRepository.save(any(MetodoPago.class)))
                .thenThrow(new DataAccessResourceFailureException(
                        "Error simulado de MongoDB"));

        assertThrows(BusinessException.class,
                () -> metodoPagoService.registrarMetodoPago(
                        "cliente123", request));
    }

    @Test
    void listarMetodosPago_ClienteValido_DevuelveLista() {
        MetodoPago metodo = new MetodoPago();
        metodo.setId("pago123");
        metodo.setClienteId("cliente123");
        metodo.setTipo(TipoMetodoPago.TARJETA);
        metodo.setAlias("Mi tarjeta");
        metodo.setReferencia("**** 1234");

        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(cliente));

        when(metodoPagoRepository.findByClienteId("cliente123"))
                .thenReturn(List.of(metodo));

        List<MetodoPagoResponseDTO> resultado =
                metodoPagoService.listarMetodosPago("cliente123");

        assertEquals(1, resultado.size());
        assertEquals("pago123", resultado.get(0).id());
        assertEquals("Mi tarjeta", resultado.get(0).alias());
    }

    @Test
    void listarMetodosPago_ErrorMongoDB_LanzaBusinessException() {
        when(usuarioRepository.findById("cliente123"))
                .thenReturn(Optional.of(cliente));

        when(metodoPagoRepository.findByClienteId("cliente123"))
                .thenThrow(new DataAccessResourceFailureException(
                        "Error simulado de MongoDB"));

        assertThrows(BusinessException.class,
                () -> metodoPagoService.listarMetodosPago("cliente123"));
    }
}
