package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Model.Categoria;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.CategoriaService;
import esi.grupo5.esiBuy.Service.JwtService;

@WebMvcTest(CategoriaConsultaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void listarCategorias_devuelve200ConLaLista() throws Exception {
        when(categoriaService.listarCategorias()).thenReturn(List.of(
                new Categoria("c1", "Hogar"),
                new Categoria("c2", "Ropa")));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("c1"))
                .andExpect(jsonPath("$[0].nombre").value("Hogar"))
                .andExpect(jsonPath("$[1].nombre").value("Ropa"));
    }

    @Test
    void listarCategorias_sinCategoriasDevuelve200ConListaVacia() throws Exception {
        when(categoriaService.listarCategorias()).thenReturn(List.of());

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listarCategorias_noExponeUnaRutaAdministrativaDuplicada() throws Exception {
        mockMvc.perform(get("/admin/categorias"))
                .andExpect(status().isNotFound());
    }

}
