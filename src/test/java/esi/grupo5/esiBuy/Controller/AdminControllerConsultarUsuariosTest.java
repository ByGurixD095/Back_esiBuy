package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.UserDto;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerConsultarUsuariosTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminUserController(userService))
                .build();
    }

    @Test
    void getUsers_devuelveListaDeUsuariosSinDatosSensibles() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserDto("id-1", "Ana", "Pérez", "ana@test.com", Rol.CLIENTE, true, false),
                new UserDto("id-2", "Luis", "Díaz", "luis@test.com", Rol.VENDEDOR, false, true)
        ));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].apellidos").value("Pérez"))
                .andExpect(jsonPath("$[0].email").value("ana@test.com"))
                .andExpect(jsonPath("$[0].rol").value("CLIENTE"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].bloqueado").value(false))
                .andExpect(jsonPath("$[0].contrasena").doesNotExist());

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_devuelveUsuarioSolicitado() throws Exception {
        when(userService.getUserById("id-1")).thenReturn(
                new UserDto("id-1", "Ana", "Pérez", "ana@test.com", Rol.ADMINISTRADOR, true, false)
        );

        mockMvc.perform(get("/api/admin/usuarios/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.apellidos").value("Pérez"))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        verify(userService).getUserById("id-1");
    }
}
