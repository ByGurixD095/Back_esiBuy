package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Service.AdminService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerConsultaUsuariosEndpointTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private JwtService jwtService;

    @Test
    void consultarUsuarios_devuelveListado() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserDto("cliente-1", "Ana", "López", "ana@test.com",
                        Rol.CLIENTE, true, false)));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cliente-1"))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].apellidos").value("López"))
                .andExpect(jsonPath("$[0].email").value("ana@test.com"))
                .andExpect(jsonPath("$[0].rol").value("CLIENTE"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].bloqueado").value(false));

        verify(userService).getAllUsers();
    }

    @Test
    void consultarUsuarios_sinUsuariosDevuelveListaVacia() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService).getAllUsers();
    }

    @Test
    void consultarUsuarioPorId_devuelveElUsuarioSolicitado() throws Exception {
        when(userService.getUserById("cliente-1")).thenReturn(
                new UserDto("cliente-1", "Ana", "López", "ana@test.com",
                        Rol.CLIENTE, true, false));

        mockMvc.perform(get("/admin/{id}", "cliente-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cliente-1"))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        verify(userService).getUserById("cliente-1");
    }
}
