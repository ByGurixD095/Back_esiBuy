package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Dto.ProductInfoDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.CategoriaRepository;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserService usuarioService;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void configurarCategoria() {
        lenient().when(categoriaRepository.findById("categoria-1"))
                .thenReturn(Optional.of(new Categoria("categoria-1", "Ropa")));
    }

    @Test
    void crearProducto_copiaLosCamposYLoActivaPorDefecto() {
        ProductoDTO dto = productoDTO(10, 1999, 5, 10, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto, "vendedor-1");

        assertEquals("Camiseta ESI", resultado.getNombre());
        assertEquals("REF-092026a", resultado.getReferencia());
        assertEquals(10, resultado.getNumStock());
        assertEquals(1999, resultado.getPrecioCent());
        assertEquals("Camiseta oficial", resultado.getDescripcion());
        assertEquals("categoria-1", resultado.getCategoriaId());
        assertEquals("Ropa", resultado.getCategoria());
        assertEquals("url_imagen.jpg", resultado.getUrlImagen());
        assertEquals("vendedor-1", resultado.getIdVendedor());
        assertEquals(5, resultado.getDescuento());
        assertEquals(10, resultado.getDescuentoPremium());
        assertTrue(resultado.isActivo());
    }

    @Test
    void crearProducto_stockNuloLoInicializaA0() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(productoDTO(null, 1999, 5, 10, null), "vendedor-1");

        assertEquals(0, resultado.getNumStock());
    }

    @Test
    void crearProducto_respetaElEstadoInactivoSolicitado() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(productoDTO(10, 1999, 5, 10, false), "vendedor-1");

        assertFalse(resultado.isActivo());
    }

    @Test
    void crearProducto_permiteDescuentosDel100PorCienYValoresNumericosMaximos() {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(
                productoDTO(Integer.MAX_VALUE, Integer.MAX_VALUE, 100, 100, true), "vendedor-1");

        assertEquals(Integer.MAX_VALUE, resultado.getNumStock());
        assertEquals(Integer.MAX_VALUE, resultado.getPrecioCent());
        assertEquals(100, resultado.getDescuento());
        assertEquals(100, resultado.getDescuentoPremium());
    }

    @Test
    void crearProducto_preservaCamposOpcionalesNulosYCeros() {
        ProductoDTO dto = new ProductoDTO(
                "Camiseta ESI", "REF-092026a", 0, null, "categoria-1", null, 0, 0, 0, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto, "vendedor-1");

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
                "Camiseta ESI", "REF-092026a", 1999, null, "categoria-1", null, null, null, null, null);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto, "vendedor-1");

        assertEquals(0, resultado.getDescuento());
        assertEquals(0, resultado.getDescuentoPremium());
    }

    @Test
    void obtenerProducto_soloDevuelveProductoDelVendedor() {
        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setIdVendedor("vendedor-1");
        when(productoRepository.findByIdAndIdVendedor("producto-1", "vendedor-1"))
                .thenReturn(Optional.of(producto));

        assertEquals(producto, productoService.obtenerProducto("producto-1", "vendedor-1"));
    }

    @Test
    void infoProducto_soloBuscaProductosActivosYDevuelveElNombreComercial() {
        Producto producto = new Producto(
                "Camiseta ESI", "REF-1", 2, 1999, "Descripción", "categoria-1", "imagen.jpg", 5, 10);
        producto.setIdVendedor("vendedor-1");
        when(productoRepository.findById("producto-1")).thenReturn(Optional.of(producto));
        when(usuarioService.getNombreComercialById("vendedor-1")).thenReturn("Tienda ESI");

        ProductInfoDTO resultado = productoService.infoProducto("producto-1");

        assertEquals("Camiseta ESI", resultado.nombre());
        assertEquals(2, resultado.numStock());
        assertEquals(1999, resultado.precioCent());
        assertEquals("Descripción", resultado.descripcion());
        assertEquals("Ropa", resultado.categoria());
        assertEquals("imagen.jpg", resultado.urlImagen());
        assertEquals(5, resultado.descuento());
        assertEquals(10, resultado.descuentoPremium());
        assertEquals("Tienda ESI", resultado.vendedor());
        verify(productoRepository).findById("producto-1");
        verify(usuarioService).getNombreComercialById("vendedor-1");
    }

    @Test
    void infoProducto_noDevuelveProductosInactivos() {
        Producto producto = new Producto();
        producto.setActivo(false);
        when(productoRepository.findById("producto-inactivo")).thenReturn(Optional.of(producto));

        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> productoService.infoProducto("producto-inactivo"));
        verify(productoRepository).findById("producto-inactivo");
        org.mockito.Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    void infoProducto_noDevuelveProductosInexistentes() {
        when(productoRepository.findById("producto-inexistente")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> productoService.infoProducto("producto-inexistente"));
        org.mockito.Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    void obtenerCatalogoVendedor_incluyeProductosInactivos() {
        Producto productoInactivo = new Producto();
        productoInactivo.setActivo(false);
        when(productoRepository.findByIdVendedor("vendedor-1", PageRequest.of(0, 36)))
                .thenReturn(new PageImpl<>(List.of(productoInactivo)));

        Page<Producto> resultado = productoService.obtenerCatalogoVendedor("vendedor-1", 0, 36);

        assertEquals(1, resultado.getTotalElements());
        assertFalse(resultado.getContent().get(0).isActivo());
        verify(productoRepository).findByIdVendedor("vendedor-1", PageRequest.of(0, 36));
    }

    @Test
    void actualizarProducto_noPermiteEditarProductoDeOtroVendedor() {
        when(productoRepository.findByIdAndIdVendedor("producto-1", "vendedor-2"))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                esi.grupo5.esiBuy.Exception.NotFoundException.class,
                () -> productoService.actualizarProducto(
                        "producto-1", productoDTO(10, 1999, 5, 10, true), "vendedor-2"));
    }

    @Test
    void eliminarProducto_realizaBorradoFisico() {
        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setIdVendedor("vendedor-1");
        when(productoRepository.findByIdAndIdVendedor("producto-1", "vendedor-1"))
                .thenReturn(Optional.of(producto));
        
        productoService.eliminarProducto("producto-1", "vendedor-1");
        
        verify(productoRepository).delete(producto);
    }

    @Test
    void obtenerProductosDisponibles_aplicaFiltrosPaginacionOrdenYBusquedaLiteral() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                "camiseta.*", "categoria-1", 1000, 5000, true, null);
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
        assertEquals("categoria-1", query.getQueryObject().get("categoriaId"));
    }

    @Test
    void obtenerProductosDisponibles_priorizaElOrdenDelCatalogoSobreElDePaginacion() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                null, "categoria-1", null, null, null, "Precio Ascendente");
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
                "categoria-1", "url_imagen.jpg", stock, descuento, descuentoPremium, activo);
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
