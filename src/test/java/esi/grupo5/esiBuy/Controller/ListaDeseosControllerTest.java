package esi.grupo5.esiBuy.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import esi.grupo5.esiBuy.Model.ListaDeseos;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.ListaDeseosService;

@WebMvcTest(ListaDeseosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListaDeseosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaDeseosService listaDeseosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void crearLista_datosValidos_devuelve201YAsociaAlClienteAutenticado() throws Exception {
        ListaDeseos listaCreada = new ListaDeseos();
        listaCreada.setId("lista-1");
        listaCreada.setIdCliente("cliente-1");
        listaCreada.setNombre("Para comprar");

        when(listaDeseosService.crearLista("Para comprar", "cliente-1"))
                .thenReturn(listaCreada);

        mockMvc.perform(post("/listas-deseos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Para comprar"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("lista-1"))
                .andExpect(jsonPath("$.idCliente").value("cliente-1"))
                .andExpect(jsonPath("$.nombre").value("Para comprar"));

        verify(listaDeseosService).crearLista("Para comprar", "cliente-1");
    }

    @Test
    void crearLista_nombreVacio_devuelve400YSinInvocarServicio() throws Exception {
        mockMvc.perform(post("/listas-deseos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": ""}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(listaDeseosService);
    }

    @Test
    void crearLista_nombreNulo_devuelve400YSinInvocarServicio() throws Exception {
        mockMvc.perform(post("/listas-deseos")
                        .principal(new UsernamePasswordAuthenticationToken("cliente-1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": null}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(listaDeseosService);
    }
}
