package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Dto.ItemCarritoResponseDTO;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CarritoService;
import esi.grupo5.esiBuy.Service.JwtService;

@WebMvcTest(CarritoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void listarCarrito_devuelveProductosCliente() throws Exception {
        ItemCarritoResponseDTO item = new ItemCarritoResponseDTO("p1","Ratón inalámbrico",1999,"raton.jpg",3);

        when(carritoService.listarCarrito("cliente-1")).thenReturn(List.of(item));

        mockMvc.perform(get("/carrito").principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].productoId").value("p1"))
            .andExpect(jsonPath("$[0].nombre").value("Ratón inalámbrico"))
            .andExpect(jsonPath("$[0].precioCent").value(1999))
            .andExpect(jsonPath("$[0].urlImagen").value("raton.jpg"))
            .andExpect(jsonPath("$[0].cantidad").value(3));

        verify(carritoService).listarCarrito("cliente-1");
    }

    @Test
    void anadirProducto_alCarritoDevuelveSinContenido() throws Exception {
        mockMvc.perform(post("/carrito/producto-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null)))
            .andExpect(status().isNoContent());
    }

    @Test
    void actualizarCantidad_delProductoDevuelveSinContenido() throws Exception {
        mockMvc.perform(patch("/carrito/producto-1")
                .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                .contentType("application/json")
                .content("{\"cantidad\":2}"))
            .andExpect(status().isNoContent());
    }
    
}
