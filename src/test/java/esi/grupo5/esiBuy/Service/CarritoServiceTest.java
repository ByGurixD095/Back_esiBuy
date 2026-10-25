package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Repository.CarritoRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void listarCarrito_carritoVacioDevuelveListaVacia() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());

        assertTrue(carritoService.listarCarrito("cliente-1").isEmpty());

    }

}
