package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Dto.ProductoDTO;
import esi.grupo5.esiBuy.Model.Producto;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad para aislar el test del controller
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crearProducto_DatosCorrectos_Devuelve201Created() throws Exception {
        ProductoDTO dto = new ProductoDTO(
                "Camiseta ESI", "REF-092026a", 10, 1999,
                "Camiseta oficial de algodón", "Ropa", "url_imagen.jpg", 5, 0
        );

        Producto productoCreado = new Producto();
        productoCreado.setId("mongo-id-123");
        productoCreado.setNombre(dto.nombre());
        productoCreado.setReferencia(dto.referencia());
        productoCreado.setPrecioCent(dto.precioCent());
        productoCreado.setActivo(true);

        when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoCreado);

        mockMvc.perform(post("/products/createProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("mongo-id-123"))
                .andExpect(jsonPath("$.nombre").value("Camiseta ESI"))
                .andExpect(jsonPath("$.precioCent").value(1999));
    }
}