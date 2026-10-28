package esi.grupo5.esiBuy.Service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crearCategoria_categoriaValidaLaGuardaYDevuelveLaGuardada() {
        Categoria solicitada = new Categoria(null, "Ropa");
        Categoria guardada = new Categoria("categoria-1", "Ropa");
        when(categoriaRepository.save(solicitada)).thenReturn(guardada);

        Categoria resultado = categoriaService.crearCategoria(solicitada);

        assertSame(guardada, resultado);
        verify(categoriaRepository).save(solicitada);
    }

    @Test
    void crearCategoria_nombreDuplicadoIgnorandoMayusculasLanzaConflicto() {
        Categoria categoria = new Categoria(null, "  ROPA  ");
        when(categoriaRepository.existsByNombreRegexIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoriaService.crearCategoria(categoria));

        verify(categoriaRepository).existsByNombreRegexIgnoreCase("^\\QROPA\\E$");
        verify(categoriaRepository, never()).save(categoria);
    }

    @Test
    void crearCategoria_nombreConCaracteresRegexSeBuscaComoTextoLiteral() {
        Categoria categoria = new Categoria(null, "Hogar (kids)+");
        when(categoriaRepository.existsByNombreRegexIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoriaService.crearCategoria(categoria));

        verify(categoriaRepository).existsByNombreRegexIgnoreCase("^\\QHogar (kids)+\\E$");
        verify(categoriaRepository, never()).save(categoria);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "   ", "\t" })
    void crearCategoria_nombreVacioOLlenoDeEspaciosLanzaValidacion(String nombre) {
        Categoria categoria = new Categoria(null, nombre);

        assertThrows(ValidationException.class, () -> categoriaService.crearCategoria(categoria));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void crearCategoria_sinDatosLanzaValidacion() {
        assertThrows(ValidationException.class, () -> categoriaService.crearCategoria(null));
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void actualizarCategoria_cambiaNombreSinAlterarElIdentificador() {
        Categoria existente = new Categoria("c1", "Ropa");
        when(categoriaRepository.findById("c1")).thenReturn(java.util.Optional.of(existente));
        when(categoriaRepository.save(existente)).thenReturn(existente);

        Categoria resultado = categoriaService.actualizarCategoria("c1", new Categoria(null, "  Moda  "));

        org.junit.jupiter.api.Assertions.assertEquals("c1", resultado.getId());
        org.junit.jupiter.api.Assertions.assertEquals("Moda", resultado.getNombre());
        verify(categoriaRepository).existsByIdNotAndNombreRegexIgnoreCase("c1", "^\\QModa\\E$");
        verify(categoriaRepository).save(existente);
    }

    @Test
    void actualizarCategoria_nombreDuplicadoLanzaConflicto() {
        when(categoriaRepository.findById("c1"))
                .thenReturn(java.util.Optional.of(new Categoria("c1", "Ropa")));
        when(categoriaRepository.existsByIdNotAndNombreRegexIgnoreCase("c1", "^\\QModa\\E$"))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> categoriaService.actualizarCategoria("c1", new Categoria(null, "Moda")));
        verify(categoriaRepository, never()).save(org.mockito.ArgumentMatchers.any(Categoria.class));
    }

    @Test
    void actualizarCategoria_inexistenteLanzaNotFound() {
        when(categoriaRepository.findById("x")).thenReturn(java.util.Optional.empty());

        assertThrows(esi.grupo5.esiBuy.Exception.NotFoundException.class,
                () -> categoriaService.actualizarCategoria("x", new Categoria(null, "Moda")));
        verify(categoriaRepository, never()).save(org.mockito.ArgumentMatchers.any(Categoria.class));
    }

    @Test
    void actualizarCategoria_nombreVacioLanzaValidacion() {
        assertThrows(ValidationException.class,
                () -> categoriaService.actualizarCategoria("c1", new Categoria(null, "  ")));
        verifyNoInteractions(categoriaRepository);
    }
}
