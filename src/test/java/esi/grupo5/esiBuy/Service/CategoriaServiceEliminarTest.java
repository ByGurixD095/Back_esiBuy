package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceEliminarTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void eliminarCategoria_sinProductosLaElimina() {
        Categoria categoria = new Categoria("c1", "Ropa");
        when(categoriaRepository.findById("c1")).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaId("c1")).thenReturn(false);

        categoriaService.eliminarCategoria("c1");

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminarCategoria_inexistenteLanzaNotFoundYNoBorra() {
        when(categoriaRepository.findById("x")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoriaService.eliminarCategoria("x"));

        verify(categoriaRepository, never()).delete(org.mockito.ArgumentMatchers.any(Categoria.class));
    }

    @Test
    void eliminarCategoria_conProductosLanzaConflictoYNoBorra() {
        Categoria categoria = new Categoria("c1", "Ropa");
        when(categoriaRepository.findById("c1")).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaId("c1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoriaService.eliminarCategoria("c1"));

        verify(categoriaRepository, never()).delete(categoria);
    }
}