package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Favorito;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.FavoritoRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    @Test
    void marcarFavorito_productoActivoCreaUnFavoritoVigente() {
        when(productoRepository.findById("p1")).thenReturn(Optional.of(producto("p1", true)));
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1")).thenReturn(Optional.empty());

        favoritoService.marcarFavorito("c1", "p1");

        ArgumentCaptor<Favorito> captor = ArgumentCaptor.forClass(Favorito.class);
        verify(favoritoRepository).save(captor.capture());
        assertEquals("c1", captor.getValue().getClienteId());
        assertEquals("p1", captor.getValue().getProductoId());
        assertTrue(captor.getValue().isActivo());
    }

    @Test
    void marcarFavorito_siYaEstabaMarcadoNoLoDuplica() {
        when(productoRepository.findById("p1")).thenReturn(Optional.of(producto("p1", true)));
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1"))
                .thenReturn(Optional.of(favorito("c1", "p1", true, LocalDateTime.now())));

        favoritoService.marcarFavorito("c1", "p1");

        verify(favoritoRepository, never()).save(any(Favorito.class));
    }

    @Test
    void marcarFavorito_siLoHabiaQuitadoSeReactiva() {
        Favorito quitado = favorito("c1", "p1", false, LocalDateTime.now());
        when(productoRepository.findById("p1")).thenReturn(Optional.of(producto("p1", true)));
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1")).thenReturn(Optional.of(quitado));

        favoritoService.marcarFavorito("c1", "p1");

        ArgumentCaptor<Favorito> captor = ArgumentCaptor.forClass(Favorito.class);
        verify(favoritoRepository).save(captor.capture());
        assertTrue(captor.getValue().isActivo());
    }

    @Test
    void marcarFavorito_productoInexistenteLanzaNotFoundYNoGuarda() {
        when(productoRepository.findById("nope")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> favoritoService.marcarFavorito("c1", "nope"));

        verifyNoInteractions(favoritoRepository);
    }

    @Test
    void marcarFavorito_productoInactivoLanzaNotFoundYNoGuarda() {
        when(productoRepository.findById("p1")).thenReturn(Optional.of(producto("p1", false)));

        assertThrows(NotFoundException.class, () -> favoritoService.marcarFavorito("c1", "p1"));

        verifyNoInteractions(favoritoRepository);
    }

    @Test
    void marcarFavorito_siOtraPeticionLoCreoAntesSeIgnoraElDuplicado() {
        when(productoRepository.findById("p1")).thenReturn(Optional.of(producto("p1", true)));
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1")).thenReturn(Optional.empty());
        when(favoritoRepository.save(any(Favorito.class))).thenThrow(new DuplicateKeyException("duplicado"));

        assertDoesNotThrow(() -> favoritoService.marcarFavorito("c1", "p1"));
    }

    @Test
    void quitarFavorito_vigenteSeDesactivaSinBorrarlo() {
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1"))
                .thenReturn(Optional.of(favorito("c1", "p1", true, LocalDateTime.now())));

        favoritoService.quitarFavorito("c1", "p1");

        ArgumentCaptor<Favorito> captor = ArgumentCaptor.forClass(Favorito.class);
        verify(favoritoRepository).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        verify(favoritoRepository, never()).delete(any(Favorito.class));
    }

    @Test
    void quitarFavorito_inexistenteNoHaceNada() {
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> favoritoService.quitarFavorito("c1", "p1"));

        verify(favoritoRepository, never()).save(any(Favorito.class));
    }

    @Test
    void quitarFavorito_yaQuitadoNoVuelveAGuardar() {
        when(favoritoRepository.findByClienteIdAndProductoId("c1", "p1"))
                .thenReturn(Optional.of(favorito("c1", "p1", false, LocalDateTime.now())));

        favoritoService.quitarFavorito("c1", "p1");

        verify(favoritoRepository, never()).save(any(Favorito.class));
    }

    @Test
    void listarFavoritos_ordenaDelMasRecienteAlMasAntiguoYOmiteProductosInactivos() {
        Favorito antiguo = favorito("c1", "p1", true, LocalDateTime.of(2026, 10, 1, 10, 0));
        Favorito reciente = favorito("c1", "p2", true, LocalDateTime.of(2026, 10, 2, 10, 0));
        Favorito deProductoInactivo = favorito("c1", "p3", true, LocalDateTime.of(2026, 10, 3, 10, 0));
        when(favoritoRepository.findByClienteIdAndActivoTrue("c1"))
                .thenReturn(List.of(antiguo, reciente, deProductoInactivo));
        when(productoRepository.findAllById(any()))
                .thenReturn(List.of(producto("p1", true), producto("p2", true), producto("p3", false)));

        List<Producto> resultado = favoritoService.listarFavoritos("c1");

        assertEquals(List.of("p2", "p1"), resultado.stream().map(Producto::getId).toList());
    }

    @Test
    void listarFavoritos_omiteLosProductosQueYaNoExisten() {
        when(favoritoRepository.findByClienteIdAndActivoTrue("c1"))
                .thenReturn(List.of(favorito("c1", "p1", true, LocalDateTime.now())));
        when(productoRepository.findAllById(any())).thenReturn(List.of());

        assertTrue(favoritoService.listarFavoritos("c1").isEmpty());
    }

    @Test
    void listarFavoritos_sinFavoritosDevuelveListaVacia() {
        when(favoritoRepository.findByClienteIdAndActivoTrue("c1")).thenReturn(List.of());
        when(productoRepository.findAllById(any())).thenReturn(List.of());

        assertTrue(favoritoService.listarFavoritos("c1").isEmpty());
    }

    private Producto producto(String id, boolean activo) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setActivo(activo);
        return producto;
    }

    private Favorito favorito(String clienteId, String productoId, boolean activo, LocalDateTime fecha) {
        Favorito favorito = new Favorito(clienteId, productoId);
        favorito.setActivo(activo);
        favorito.setFechaAlta(fecha);
        return favorito;
    }
}