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
    void crearProducto_DtoNulo_LanzaExcepcion() {
        assertDtoInvalido(null);
    }

    @Test
    void crearProducto_StockNegativo_LanzaExcepcion() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", -1, 1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg");

        assertDtoInvalido(dto);
    }

    @Test
    void crearProducto_PrecioNegativo_LanzaExcepcion() {
        ProductoDTO dto = dtoConValores("Camiseta ESI", "REF-092026a", 10, -1999,
                "Camiseta oficial", "Ropa", "url_imagen.jpg");

        assertDtoInvalido(dto);
    }

    @Test
    void crearProducto_ReferenciaNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), null, dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen()));
        assertDtoInvalido(dtoConValores(dto.nombre(), "", dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen()));
    }

    @Test
    void crearProducto_CategoriaNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), null, dto.urlImagen()));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), "", dto.urlImagen()));
    }

    @Test
    void crearProducto_DescripcionNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                null, dto.categoria(), dto.urlImagen()));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                "", dto.categoria(), dto.urlImagen()));
    }

    @Test
    void crearProducto_UrlImagenNulaOVacia_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), null));
        assertDtoInvalido(dtoConValores(dto.nombre(), dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), ""));
    }

    @Test
    void crearProducto_NombreNuloOVacio_LanzaExcepcion() {
        ProductoDTO dto = dtoValido();

        assertDtoInvalido(dtoConValores(null, dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen()));
        assertDtoInvalido(dtoConValores("", dto.referencia(), dto.numStock(), dto.precioCent(),
                dto.descripcion(), dto.categoria(), dto.urlImagen()));
    }

    private ProductoDTO dtoValido() {
        return dtoConValores(
                "Camiseta ESI",
                "REF-092026a",
                10,
                1999,
                "Camiseta oficial de la cervezada de la ESI 2026",
                "Ropa",
                "url_imagen.jpg"
        );
    }

    private ProductoDTO dtoConValores(String nombre, String referencia, int numStock,
                                      int precioCent, String descripcion, String categoria,
                                      String urlImagen) {
        return new ProductoDTO(nombre, referencia, numStock, precioCent,
                descripcion, categoria, urlImagen);
    }

    private void assertDtoInvalido(ProductoDTO dto) {
        assertThrows(IllegalArgumentException.class,
                () -> productoService.crearProducto(dto));
    }
}