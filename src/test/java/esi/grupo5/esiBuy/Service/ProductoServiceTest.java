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
        // 1. ARRANGE (Preparación de datos y simulación del repositorio)
        ProductoDTO dto = new ProductoDTO(
            "Camiseta ESI", 
            "REF-092026a", 
            10, 
            1999, // 19.99 € en céntimos
            "Camiseta oficial de la cervezada de la ESI 2026", 
            "Ropa", 
            "url_imagen.jpg"
        );

        Producto productoSimulado = new Producto();
        productoSimulado.setId("mongo-id-123");
        productoSimulado.setNombre(dto.nombre());
        productoSimulado.setReferencia(dto.referencia());
        productoSimulado.setNumStock(dto.numStock());
        productoSimulado.setPrecioCent(dto.precioCent());
        productoSimulado.setActivo(true); // Por defecto nace activo para el borrado lógico

        when(productoRepository.save(any(Producto.class))).thenReturn(productoSimulado);

        // 2. ACT (Ejecución del método que vamos a programar)
        Producto resultado = productoService.crearProducto(dto);

        // 3. ASSERT (Comprobaciones)
        assertNotNull(resultado, "El producto creado no debe ser nulo");
        assertEquals("Camiseta ESI", resultado.getNombre());
        assertEquals("REF-092026a", resultado.getReferencia());
        assertEquals(1999, resultado.getPrecioCent());
        assertTrue(resultado.isActivo(), "El producto debe nacer activo");
    }
}