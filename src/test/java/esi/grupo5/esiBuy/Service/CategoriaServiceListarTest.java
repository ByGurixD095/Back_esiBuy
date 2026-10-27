package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceListarTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void listarCategorias_devuelveTodasOrdenadasPorNombreSinDistinguirMayusculas() {
        when(categoriaRepository.findAll()).thenReturn(List.of(
                new Categoria("3", "ropa"),
                new Categoria("1", "Hogar"),
                new Categoria("2", "Electrónica")));

        List<Categoria> resultado = categoriaService.listarCategorias();

        assertEquals(List.of("Electrónica", "Hogar", "ropa"),
                resultado.stream().map(Categoria::getNombre).toList());
    }

    @Test
    void listarCategorias_sinCategoriasDevuelveListaVacia() {
        when(categoriaRepository.findAll()).thenReturn(List.of());

        assertTrue(categoriaService.listarCategorias().isEmpty());
    }

    @Test
    void listarCategorias_colocaAlFinalLasQueNoTienenNombre() {
        when(categoriaRepository.findAll()).thenReturn(List.of(
                new Categoria("1", null),
                new Categoria("2", "Ropa")));

        List<Categoria> resultado = categoriaService.listarCategorias();

        assertEquals("Ropa", resultado.get(0).getNombre());
        assertEquals("1", resultado.get(1).getId());
    }
}
