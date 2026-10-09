package esi.grupo5.esiBuy.Service;

import java.util.Collections;
import java.util.List;

import esi.grupo5.esiBuy.Dto.FiltroCatalogoDTO;
import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

        @Mock
        private MongoTemplate mongoTemplate;

        @InjectMocks
        private ProductoService productoService;

        private Producto productoDisponible1;
        private Producto productoDisponible2;


    @Test
    void crearProducto_ConservaTodosLosAtributosYActivo() {
        ProductoDTO dto = new ProductoDTO(
                "Portátil ESI", "TEC-001", 89900,
                "Portátil ligero", "Tecnología", "url1.png", 7, 15, 25, false);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertAll(
                () -> assertEquals("Portátil ESI", resultado.getNombre()),
                () -> assertEquals("TEC-001", resultado.getReferencia()),
                () -> assertEquals(7, resultado.getNumStock()),
                () -> assertEquals(89900, resultado.getPrecioCent()),
                () -> assertEquals("Portátil ligero", resultado.getDescripcion()),
                () -> assertEquals("Tecnología", resultado.getCategoria()),
                () -> assertEquals("url1.png", resultado.getUrlImagen()),
                () -> assertEquals(15, resultado.getDescuento()),
                () -> assertEquals(25, resultado.getDescuentoPremium()),
                () -> assertFalse(resultado.isActivo()));
    }

    @Test
    void descuentoDel100PorCien_EsValido() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", Integer.MAX_VALUE,
                Integer.MAX_VALUE, "Camiseta oficial", "Ropa", "url_imagen.jpg", 100, 100);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(100, resultado.getDescuento());
        assertEquals(100, resultado.getDescuentoPremium());
    }

    @Test
    void valoresNumericosMaximosDeInt_SonValidos() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", Integer.MAX_VALUE,
                Integer.MAX_VALUE, "Camiseta oficial", "Ropa", "url_imagen.jpg", 0, 0);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(Integer.MAX_VALUE, resultado.getNumStock());
        assertEquals(Integer.MAX_VALUE, resultado.getPrecioCent());
    }

    @Test
    void stockNulo_SeInicializaA0() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", null, 1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", 5, 10);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(0, resultado.getNumStock());
    }

    @Test
    void descripcionNula_EsValida() {
        ProductoDTO dto = dtoValido();

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dtoConValores(dto.nombre(), dto.referencia(),
                dto.numStock(), dto.precioCent(), null, dto.categoria(), dto.urlImagen(), 5, 10));

        assertNotNull(resultado);
    }

    @Test
    void descripcionVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                "", dto.categoria(), dto.urlImagen(), 5, 10));
    }



    @Test
    void precioNegativo_LanzaExcepcion() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", 10, -1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", 5, 10);

        assertDtoInvalido(dto);
    }

    @Test
    void referenciaNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), null, dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
        assertDtoInvalido(dtoConValores(dto.nombre(), "", dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
    }

    @Test
    void urlImagenNula_EsValida() {
        ProductoDTO dto = dtoValido();

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dtoConValores(dto.nombre(), dto.referencia(),
                dto.numStock(), dto.precioCent(), dto.descripcion(), dto.categoria(), null, 5, 10));

        assertNotNull(resultado);
    }

    @Test
    void descuento_ConservaDescuento() {
        ProductoDTO dto = dtoConDescuentos(10, 0);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(10, resultado.getDescuento());
    }

    @Test
    void descuentoPremium_ConservaDescuentoPremium() {
        ProductoDTO dto = dtoConDescuentos(0, 25);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(25, resultado.getDescuentoPremium());
    }

    @Test
    void valorCeroEnCamposNumericos_SonValidos() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", 0, 0,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", 0, 0);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertNotNull(resultado);
    }

    private ProductoDTO dtoValido() {
        return dtoConValores(
                "Camiseta ESI",
                "REF-092026a",
                10,
                1999,
                "Camiseta oficial de la cervezada de la ESI 2026",
                "Ropa",
                "url_imagen.jpg",
                5,
                10
        );
    }

        private ProductoDTO dtoConValores(String nombre, String referencia, Integer numStock,
                                      int precioCent, String descripcion, String categoria,
                                      String urlImagen, int descuento, int descuentoPremium) {
        return new ProductoDTO(nombre, referencia, precioCent,
                descripcion, categoria, urlImagen, numStock, descuento, descuentoPremium, null);
    }

    private ProductoDTO dtoConDescuentos(int descuento, int descuentoPremium) {
        return dtoConValores("Camiseta ESI", "REF-092026a", 10, 1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", descuento, descuentoPremium);
    }

    @BeforeEach
    void setUp() {
        productoDisponible1 = new Producto("Portátil ESI", "TEC-001", 10, 89900, "Portátil ligero", "Tecnología", "url1.png", 0, 0);
        productoDisponible1.setId("prod-1");
        productoDisponible1.setActivo(true);

        productoDisponible2 = new Producto("Mochila ESI", "ESC-002", 5, 2990, "Mochila ergonómica", "Papelería y Material Escolar", "url2.png", 0, 0);
        productoDisponible2.setId("prod-2");
        productoDisponible2.setActivo(true);
    }

        @Test
        @DisplayName("TDD: aplica todos los filtros del catálogo a la consulta MongoDB")
        void obtenerProductosDisponibles_aplicaFiltrosDelCatalogo() {
                when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                                .thenReturn(List.of(productoDisponible1));

                FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                                "camiseta", "Ropa", 1000, 5000, true, "Precio Ascendente");

                List<Producto> resultado = productoService.obtenerProductosDisponibles(filtros);

                ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
                verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));

                String query = queryCaptor.getValue().getQueryObject().toJson();
                assertEquals(List.of(productoDisponible1), resultado);
                assertTrue(query.contains("activo"));
                assertTrue(query.contains("numStock"));
                assertTrue(query.contains("categoria"));
                assertTrue(query.contains("precioCent"));
                assertTrue(query.contains("descuento"));
                assertTrue(query.contains("camiseta"));
                assertTrue(queryCaptor.getValue().getSortObject().containsKey("precioCent"));
        }

        @Test
        @DisplayName("TDD: sin filtros mantiene las reglas de productos activos con stock")
        void obtenerProductosDisponibles_sinFiltros_aplicaReglasBase() {
                when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                                .thenReturn(Collections.emptyList());

                productoService.obtenerProductosDisponibles(null);

                ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
                verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));

                String query = queryCaptor.getValue().getQueryObject().toJson();
                assertTrue(query.contains("activo"));
                assertTrue(query.contains("numStock"));
        }

    @Test
    @DisplayName("TDD: filtra por categoría sin añadir otros criterios opcionales")
    void obtenerProductosDisponibles_filtraSoloPorCategoria() {
        when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                .thenReturn(Collections.emptyList());

        productoService.obtenerProductosDisponibles(
                new FiltroCatalogoDTO(null, "Ropa", null, null, false, null));

        ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        String query = queryCaptor.getValue().getQueryObject().toJson();

        assertTrue(query.contains("Ropa"));
        assertFalse(query.contains("descuento"));
        assertFalse(query.contains("precioCent"));
    }

    @Test
    @DisplayName("TDD: ignora la categoría Todas")
    void obtenerProductosDisponibles_ignoraCategoriaTodas() {
        when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                .thenReturn(Collections.emptyList());

        productoService.obtenerProductosDisponibles(
                new FiltroCatalogoDTO(null, "Todas", null, null, null, null));

        ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        assertFalse(queryCaptor.getValue().getQueryObject().toJson().contains("categoria"));
    }

    @Test
    @DisplayName("TDD: aplica únicamente los límites de precio recibidos")
    void obtenerProductosDisponibles_aplicaRangoDePrecio() {
        when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                .thenReturn(Collections.emptyList());

        productoService.obtenerProductosDisponibles(
                new FiltroCatalogoDTO(null, null, 1000, null, null, null));

        ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        String query = queryCaptor.getValue().getQueryObject().toJson();

        assertTrue(query.contains("precioCent"));
        assertTrue(query.contains("1000"));
    }

    @Test
    @DisplayName("TDD: ordena por precio descendente cuando se solicita")
    void obtenerProductosDisponibles_ordenaPrecioDescendente() {
        when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                .thenReturn(Collections.emptyList());

        productoService.obtenerProductosDisponibles(
                new FiltroCatalogoDTO(null, null, null, null, null, "Precio Descendente"));

        ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));

        assertEquals(-1, queryCaptor.getValue().getSortObject().get("precioCent"));
    }

    @Test
    @DisplayName("TDD: rechaza un precio mínimo mayor que el máximo")
    void obtenerProductosDisponibles_rechazaRangoInvertido() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                null, null, 5000, 1000, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> productoService.obtenerProductosDisponibles(filtros));
        verify(mongoTemplate, org.mockito.Mockito.never())
                .find(any(Query.class), eq(Producto.class));
    }

    @Test
    @DisplayName("TDD: rechaza precios negativos")
    void obtenerProductosDisponibles_rechazaPrecioNegativo() {
        FiltroCatalogoDTO filtros = new FiltroCatalogoDTO(
                null, null, -1, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> productoService.obtenerProductosDisponibles(filtros));
        verify(mongoTemplate, org.mockito.Mockito.never())
                .find(any(Query.class), eq(Producto.class));
    }

    @Test
    @DisplayName("TDD: trata la búsqueda como texto literal")
    void obtenerProductosDisponibles_escapaCaracteresDeBusqueda() {
        when(mongoTemplate.find(any(Query.class), eq(Producto.class)))
                .thenReturn(Collections.emptyList());

        productoService.obtenerProductosDisponibles(
                new FiltroCatalogoDTO("[camiseta]", null, null, null, null, null));

        ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Producto.class));
        String query = queryCaptor.getValue().getQueryObject().toJson();

        assertTrue(query.contains("\\Q[camiseta]\\E"));
    }


    @Test
    @DisplayName("RED: Debe devolver únicamente los productos activos y con stock mayor que 0")
    void obtenerProductosDisponibles_debeRetornarSoloDisponibles() {
        // GIVEN: El repositorio devuelve productos que cumplen activo=true y numStock > 0
        when(productoRepository.findByActivoTrueAndNumStockGreaterThan(0))
                .thenReturn(List.of(productoDisponible1, productoDisponible2));

        // WHEN: El servicio solicita el catálogo disponible
        List<Producto> resultado = productoService.obtenerProductosDisponibles();

        // THEN: Se valida la lista y que se invoque al método de filtrado estricto
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.isActivo() && p.getNumStock() > 0));
        verify(productoRepository, times(1)).findByActivoTrueAndNumStockGreaterThan(0);
    }

    @Test
    @DisplayName("RED: Debe devolver una lista vacía sin errores si no hay productos disponibles")
    void obtenerProductosDisponibles_cuandoNoHayStock_debeRetornarListaVacia() {
        // GIVEN: Ningún producto en BBDD cumple el criterio de disponibilidad
        when(productoRepository.findByActivoTrueAndNumStockGreaterThan(0))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Producto> resultado = productoService.obtenerProductosDisponibles();

        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(productoRepository, times(1)).findByActivoTrueAndNumStockGreaterThan(0);
    }

}