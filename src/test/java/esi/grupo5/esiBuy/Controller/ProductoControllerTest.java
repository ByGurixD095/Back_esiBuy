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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

        @Test
        void crearProducto_PrecioFueraDeRango_Devuelve400BadRequest() throws Exception {
                String dtoConPrecioDemasiadoGrande = """
                                {
                                    "nombre": "Camiseta ESI",
                                    "referencia": "REF-092026a",
                                    "numStock": 10,
                                    "precioCent": 80000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                    "descripcion": "Camiseta oficial de algodón",
                                    "categoria": "Ropa",
                                    "urlImagen": "url_imagen.jpg",
                                    "descuento": 5,
                                    "descuentoPremium": 0
                                }
                                """;

                mockMvc.perform(post("/products/createProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(dtoConPrecioDemasiadoGrande))
                                .andExpect(status().isBadRequest());
        }

                @Test
                void crearProducto_StockFueraDeRango_Devuelve400BadRequest() throws Exception {
                String dtoConStockDemasiadoGrande = """
                    {
                      "nombre": "Camiseta ESI",
                      "referencia": "REF-092026a",
                      "numStock": 80000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                      "precioCent": 1999,
                      "descripcion": "Camiseta oficial de algodón",
                      "categoria": "Ropa",
                      "urlImagen": "url_imagen.jpg",
                      "descuento": 5,
                      "descuentoPremium": 0
                    }
                    """;

                mockMvc.perform(post("/products/createProduct")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(dtoConStockDemasiadoGrande))
                    .andExpect(status().isBadRequest());
                }

                @Test
                void crearProducto_SinStock_InicializaStockA0() throws Exception {
                Producto productoCreado = new Producto();
                productoCreado.setId("mongo-id-123");
                when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoCreado);

                String dtoSinStock = """
                    {
                      "nombre": "Camiseta ESI",
                      "referencia": "REF-092026a",
                      "precioCent": 1999,
                      "descripcion": "Camiseta oficial de algodón",
                      "categoria": "Ropa",
                      "urlImagen": "url_imagen.jpg",
                      "descuento": 5,
                      "descuentoPremium": 0
                    }
                    """;

                mockMvc.perform(post("/products/createProduct")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(dtoSinStock))
                    .andExpect(status().isCreated());

                verify(productoService).crearProducto(argThat(dto -> dto.numStock() == null));
                }

            @Test
            void crearProducto_SinDescripcionNiImagen_EsAceptado() throws Exception {
            Producto productoCreado = new Producto();
            productoCreado.setId("mongo-id-123");
            when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoCreado);

            String dtoSinCamposOpcionales = """
                {
                  "nombre": "Camiseta ESI",
                  "referencia": "REF-092026a",
                  "numStock": 0,
                  "precioCent": 1999,
                  "categoria": "Ropa",
                  "descuento": 5,
                  "descuentoPremium": 0
                }
                """;

            mockMvc.perform(post("/products/createProduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dtoSinCamposOpcionales))
                .andExpect(status().isCreated());
            }
}