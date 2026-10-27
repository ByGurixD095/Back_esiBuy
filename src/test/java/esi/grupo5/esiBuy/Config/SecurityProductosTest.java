package esi.grupo5.esiBuy.Config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Controller.ProductoController;
import esi.grupo5.esiBuy.Dto.ProductInfoDTO;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.ProductoService;

@WebMvcTest(ProductoController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityProductosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void obtenerInfoProducto_esPublico() throws Exception {
        when(productoService.infoProducto("producto-1")).thenReturn(
                new ProductInfoDTO("Producto", 1, 100, "Descripción", "Categoría", "imagen", 0, 0, "Tienda"));

        mockMvc.perform(get("/products/producto-1/info"))
                .andExpect(status().isOk());
    }

    @Test
    void otrosMetodosDeLaRutaInfoSiguenProtegidos() throws Exception {
        mockMvc.perform(delete("/products/producto-1/info"))
                .andExpect(status().isUnauthorized());
    }
}
