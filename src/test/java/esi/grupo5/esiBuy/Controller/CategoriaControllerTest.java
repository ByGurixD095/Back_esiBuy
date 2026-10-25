package esi.grupo5.esiBuy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CategoriaService;
import esi.grupo5.esiBuy.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crearCategoria_devuelve201ConCategoriaCreada() throws Exception {
        Categoria categoria = new Categoria("categoria-1", "Ropa", true);
        when(categoriaService.crearCategoria(org.mockito.ArgumentMatchers.any(Categoria.class)))
                .thenReturn(categoria);

        mockMvc.perform(post("/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("categoria-1"))
                .andExpect(jsonPath("$.nombre").value("Ropa"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crearCategoria_duplicadaDevuelve409ConMensajeExplicito() throws Exception {
        when(categoriaService.crearCategoria(org.mockito.ArgumentMatchers.any(Categoria.class)))
                .thenThrow(new ConflictException("Ya existe una categoría con ese nombre"));

        mockMvc.perform(post("/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Categoria(null, "Ropa", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una categoría con ese nombre"));
    }
}
