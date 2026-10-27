package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CategoriaService;
import esi.grupo5.esiBuy.Service.JwtService;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaEliminarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void eliminarCategoria_devuelve204() throws Exception {
        mockMvc.perform(delete("/admin/categorias/c1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).eliminarCategoria("c1");
    }

    @Test
    void eliminarCategoria_inexistenteDevuelve404() throws Exception {
        doThrow(new NotFoundException("Categoría no encontrada"))
                .when(categoriaService).eliminarCategoria("x");

        mockMvc.perform(delete("/admin/categorias/x"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
    }

    @Test
    void eliminarCategoria_conProductosDevuelve409ConMensaje() throws Exception {
        doThrow(new ConflictException("No se puede eliminar la categoría porque hay productos que la usan"))
                .when(categoriaService).eliminarCategoria("c1");

        mockMvc.perform(delete("/admin/categorias/c1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("No se puede eliminar la categoría porque hay productos que la usan"));
    }
}