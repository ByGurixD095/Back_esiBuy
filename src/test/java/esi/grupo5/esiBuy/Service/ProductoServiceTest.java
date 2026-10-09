package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

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

    private ProductoDTO productoDTO(Integer stock, Integer precio, Integer descuento,
                                    Integer descuentoPremium, Boolean activo) {
        return new ProductoDTO(
                "Camiseta ESI", "REF-092026a", precio, "Camiseta oficial",
                "Ropa", "url_imagen.jpg", stock, descuento, descuentoPremium, activo);
    }
}
