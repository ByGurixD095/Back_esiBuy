
package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Config.GlobalExceptionHandler;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Service.CarritoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerEliminarTest {

    @Mock
    private CarritoService carritoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CarritoController controller =
                new CarritoController(carritoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void eliminarProducto_CarritoValido_Devuelve204() throws Exception {

        mockMvc.perform(delete("/carrito/producto123")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isNoContent());

        verify(carritoService)
                .eliminarProducto("cliente123", "producto123");
    }

    @Test
    void eliminarProducto_CarritoInexistente_Devuelve404() throws Exception {

        doThrow(new NotFoundException("Carrito no encontrado"))
                .when(carritoService)
                .eliminarProducto("cliente123", "producto123");

        mockMvc.perform(delete("/carrito/producto123")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isNotFound());

        verify(carritoService)
                .eliminarProducto("cliente123", "producto123");
    }

    @Test
    void eliminarProducto_ProductoNoExiste_Devuelve404() throws Exception {

        doThrow(new NotFoundException(
                "El producto no está en el carrito"))
                .when(carritoService)
                .eliminarProducto("cliente123", "producto999");

        mockMvc.perform(delete("/carrito/producto999")
                .principal(new UsernamePasswordAuthenticationToken(
                        "cliente123", null)))
                .andExpect(status().isNotFound());

        verify(carritoService)
                .eliminarProducto("cliente123", "producto999");
    }
}
