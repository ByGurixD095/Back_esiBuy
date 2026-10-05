package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_DatosValidos_DevuelveProductoCreado() {

                ProductoDTO dto = dtoValido();

        Producto productoSimulado = new Producto();
        productoSimulado.setId("mongo-id-123");
        productoSimulado.setNombre(dto.nombre());
        productoSimulado.setReferencia(dto.referencia());
        productoSimulado.setNumStock(dto.numStock());
        productoSimulado.setPrecioCent(dto.precioCent());
        productoSimulado.setActivo(true); // Por defecto nace activo para el borrado lógico

        when(productoRepository.save(any(Producto.class))).thenReturn(productoSimulado);

        Producto resultado = productoService.crearProducto(dto);

        assertNotNull(resultado, "El producto creado no debe ser nulo");
        assertEquals("Camiseta ESI", resultado.getNombre());
        assertEquals("REF-092026a", resultado.getReferencia());
        assertEquals(1999, resultado.getPrecioCent());
        assertTrue(resultado.isActivo(), "El producto debe nacer activo");
    }

    @Test
    void crearProducto_Descuento_ConservaDescuento() {
        ProductoDTO dto = dtoConDescuentos(10, 0);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(10, resultado.getDescuento());
    }

    @Test
    void crearProducto_DescuentoPremium_ConservaDescuentoPremium() {
        ProductoDTO dto = dtoConDescuentos(0, 25);
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crearProducto(dto);

        assertEquals(25, resultado.getDescuentoPremium());
    }

    @Test
    void crearProducto_DtoNulo_LanzaExcepcion() {
        assertDtoInvalido(null);
    }

    // CAMPOS OBLIGATORIOS? PROBAR con un par de datos

    @Test
    void crearProducto_StockNegativo_LanzaExcepcion() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", -1, 1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", 5, 10);

        assertDtoInvalido(dto);
    }

    @Test
    void crearProducto_PrecioNegativo_LanzaExcepcion() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", 10, -1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", 5, 10);

        assertDtoInvalido(dto);
    }

    @Test
    void crearProducto_ReferenciaNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), null, dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
        assertDtoInvalido(dtoConValores(dto.nombre(), "", dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
    }

    @Test
    void crearProducto_CategoriaNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), null, dto.urlImagen(), 5, 10));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), "", dto.urlImagen(), 5, 10));
    }

    @Test
    void crearProducto_DescripcionNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                null, dto.categoria(), dto.urlImagen(), 5, 10));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                "", dto.categoria(), dto.urlImagen(), 5, 10));
    }

    @Test
    void crearProducto_UrlImagenNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), null, 5, 10));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), "", 5, 10));
    }

    @Test
    void crearProducto_NombreNuloOVacio_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(null, dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
        assertDtoInvalido(dtoConValores("", dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen(), 5, 10));
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

    private ProductoDTO dtoConValores(String nombre, String referencia, int numStock,
                                      int precioCent, String descripcion, String categoria,
                                      String urlImagen, int descuento, int descuentoPremium) {
        return new ProductoDTO(nombre, referencia, numStock, precioCent,
                descripcion, categoria, urlImagen, descuento, descuentoPremium);
    }

    private ProductoDTO dtoConDescuentos(int descuento, int descuentoPremium) {
        return dtoConValores("Camiseta ESI", "REF-092026a", 10, 1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg", descuento, descuentoPremium);
    }

    private void assertDtoInvalido(ProductoDTO dto) {
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crearProducto(dto));
    }
}


// AÑADIR TESTS PARA PETAR (80000000000000000000000000000 DE PRECIO, P EJ. )