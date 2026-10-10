package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_copiaLosCamposYLoActivaPorDefecto() {
        ProductoDTO dto = productoDTO(10, 1999, 5, 10, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals("Camiseta ESI", resultado.getNombre());
        assertEquals("REF-092026a", resultado.getReferencia());
        assertEquals(10, resultado.getNumStock());
        assertEquals(1999, resultado.getPrecioCent());
        assertEquals("Camiseta oficial", resultado.getDescripcion());
        assertEquals("Ropa", resultado.getCategoria());
        assertEquals("url_imagen.jpg", resultado.getUrlImagen());
        assertEquals(5, resultado.getDescuento());
        assertEquals(10, resultado.getDescuentoPremium());
        assertTrue(resultado.isActivo());
    }

    @Test
    void crearProducto_stockNuloLoInicializaA0() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(productoDTO(null, 1999, 5, 10, null));

        assertEquals(0, resultado.getNumStock());
    }

    @Test
    void crearProducto_respetaElEstadoInactivoSolicitado() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(productoDTO(10, 1999, 5, 10, false));

        assertFalse(resultado.isActivo());
    }

    @Test
    void crearProducto_permiteDescuentosDel100PorCienYValoresNumericosMaximos() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(
                productoDTO(Integer.MAX_VALUE, Integer.MAX_VALUE, 100, 100, true));

        assertEquals(Integer.MAX_VALUE, resultado.getNumStock());
        assertEquals(Integer.MAX_VALUE, resultado.getPrecioCent());
        assertEquals(100, resultado.getDescuento());
        assertEquals(100, resultado.getDescuentoPremium());
    }

    @Test
    void crearProducto_preservaCamposOpcionalesNulosYCeros() {
        ProductoDTO dto = new ProductoDTO(
                "Camiseta ESI", "REF-092026a", 0, null, "Ropa", null, 0, 0, 0, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(0, resultado.getNumStock());
        assertEquals(0, resultado.getPrecioCent());
        assertEquals(0, resultado.getDescuento());
        assertEquals(0, resultado.getDescuentoPremium());
        assertNull(resultado.getDescripcion());
        assertNull(resultado.getUrlImagen());
    }

    @Test
    void crearProducto_inicializaDescuentosNulosACero() {
        ProductoDTO dto = new ProductoDTO(
                "Camiseta ESI", "REF-092026a", 1999, null, "Ropa", null, null, null, null, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(0, resultado.getDescuento());
        assertEquals(0, resultado.getDescuentoPremium());
    }

    @Test
    void obtenerProductosDisponibles_aplicaFiltrosPaginacionOrdenYBusquedaLiteral() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                "camiseta.*", " Ropa ", 1000, 5000, true, null);
        PageRequest pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "nombre"));
        when(mongoTemplate.count(any(Query.class), eq(Producto.class))).thenReturn(6L);
        when(mongoTemplate.find(any(Query.class), eq(Producto.class))).thenReturn(List.of());

        Page<Producto> resultado = productoService.obtenerProductosDisponibles(filtros, pageable);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        Query query = queryCaptor.getValue();

        assertTrue(resultado.isEmpty());
        assertEquals(6, resultado.getTotalElements());
        assertEquals(2, resultado.getTotalPages());
        assertEquals(1, resultado.getNumber());
        assertEquals(5, query.getLimit());
        assertEquals(5, query.getSkip());
        assertEquals(-1, query.getSortObject().get("nombre"));

        Pattern nombrePattern = findPattern(query.getQueryObject());
        assertNotNull(nombrePattern);
        assertTrue(nombrePattern.matcher("Camiseta.*").find());
        assertFalse(nombrePattern.matcher("CamisetaXX").find());
    }

    @Test
    void obtenerProductosDisponibles_priorizaElOrdenDelCatalogoSobreElDePaginacion() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                null, null, null, null, null, "Precio Ascendente");
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "nombre"));
        when(mongoTemplate.count(any(Query.class), eq(Producto.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Producto.class))).thenReturn(List.of());

        productoService.obtenerProductosDisponibles(filtros, pageable);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        Query query = queryCaptor.getValue();

        assertEquals(1, query.getSortObject().get("precioCent"));
        assertNull(query.getSortObject().get("nombre"));
    }

    private ProductoDTO productoDTO(Integer stock, Integer precio, Integer descuento,
                                    Integer descuentoPremium, Boolean activo) {
        return new ProductoDTO(
                "Camiseta ESI", "REF-092026a", precio, "Camiseta oficial",
                "Ropa", "url_imagen.jpg", stock, descuento, descuentoPremium, activo);
    }

    private Pattern findPattern(Object value) {
        if (value instanceof Pattern pattern) {
            return pattern;
        }
        if (value instanceof Map<?, ?> map) {
            for (Object nestedValue : map.values()) {
                Pattern pattern = findPattern(nestedValue);
                if (pattern != null) {
                    return pattern;
                }
            }
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object nestedValue : iterable) {
                Pattern pattern = findPattern(nestedValue);
                if (pattern != null) {
                    return pattern;
                }
            }
        }
        return null;
    }
}
