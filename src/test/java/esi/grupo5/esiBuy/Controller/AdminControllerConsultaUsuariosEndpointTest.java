package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.ClienteResponseDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorResponseDTO;
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
                ClienteResponseDTO.builder()
                        .id("cliente-1")
                        .name("Ana")
                        .apellidos("López")
                        .email("ana@test.com")
                        .telefono("600123456")
                        .imagenPerfil("perfil.png")
                        .rol(Rol.CLIENTE)
                        .activo(true)
                        .dni("12345678A")
                        .fechaNacimiento(java.time.LocalDate.of(1990, 1, 1))
                        .tipoCliente(esi.grupo5.esiBuy.Model.enums.TipoCliente.NORMAL)
                        .build(),
                ClienteResponseDTO.builder()
                        .id("cliente-eliminado")
                        .name("Luis")
                        .apellidos("Pérez")
                        .email("luis@test.com")
                        .rol(Rol.CLIENTE)
                        .bloqueado(true)
                        .eliminado(true)
                        .build(),
                AdministradorResponseDTO.builder()
                        .id("admin-1")
                        .name("Marta")
                        .email("marta@test.com")
                        .rol(Rol.ADMINISTRADOR)
                        .sede("Madrid")
                        .fechaIncorporacion(java.time.LocalDate.of(2024, 2, 1))
                        .build()));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cliente-1"))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].apellidos").value("López"))
                .andExpect(jsonPath("$[0].email").value("ana@test.com"))
                .andExpect(jsonPath("$[0].telefono").value("600123456"))
                .andExpect(jsonPath("$[0].dni").value("12345678A"))
                .andExpect(jsonPath("$[0].fechaNacimiento").value("1990-01-01"))
                .andExpect(jsonPath("$[0].tipoCliente").value("NORMAL"))
                .andExpect(jsonPath("$[0].rol").value("CLIENTE"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].eliminado").value(false))
                .andExpect(jsonPath("$[0].bloqueado").value(false))
                .andExpect(jsonPath("$[1].id").value("cliente-eliminado"))
                .andExpect(jsonPath("$[1].bloqueado").value(true))
                .andExpect(jsonPath("$[1].eliminado").value(true))
                .andExpect(jsonPath("$[2].rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$[2].sede").value("Madrid"))
                .andExpect(jsonPath("$[2].fechaIncorporacion").value("2024-02-01"));

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
        when(userService.getUserById("vendedor-1")).thenReturn(
                VendedorResponseDTO.builder()
                        .id("vendedor-1")
                        .name("Ana")
                        .apellidos("López")
                        .email("ana@test.com")
                        .telefono("600123456")
                        .imagenPerfil("perfil.png")
                        .rol(Rol.VENDEDOR)
                        .activo(true)
                        .nombreComercial("Tienda Ana")
                        .cifNif("B12345678")
                        .categoriaPrincipalId("electronica")
                        .build());

        mockMvc.perform(get("/admin/{id}", "vendedor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("vendedor-1"))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Ana"))
                .andExpect(jsonPath("$.cifNif").value("B12345678"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("electronica"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.totpSecretCifrado").doesNotExist())
                .andExpect(jsonPath("$.tokenRecuperacionContrasena").doesNotExist())
                .andExpect(jsonPath("$.historialContrasenas").doesNotExist());

        verify(userService).getUserById("vendedor-1");
    }
}
