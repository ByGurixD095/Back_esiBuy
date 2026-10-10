package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorRegistroDTO;
import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Usuario;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.strategy.AdministradorUpdateStrategy;
import esi.grupo5.esiBuy.Service.strategy.ClienteUpdateStrategy;
import esi.grupo5.esiBuy.Service.strategy.VendedorUpdateStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import esi.grupo5.esiBuy.Dto.UserPatchDTO;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Service.strategy.AdministradorUpdateStrategy;
import esi.grupo5.esiBuy.Service.strategy.ClienteUpdateStrategy;
import esi.grupo5.esiBuy.Service.strategy.VendedorUpdateStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private UserService userService;

    private AdminService service;

   @BeforeEach
    void setUp() {
        service = new AdminService(
            usuarioRepository,
            new BCryptPasswordEncoder(),
            passwordValidatorService,
            List.of(new AdministradorUpdateStrategy(), new ClienteUpdateStrategy(),new VendedorUpdateStrategy()),
        userService);
    }

    @Test
    void crearAdministrador_guardaElRolYUnaContrasenaHasheada() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.toAdministradorResponseDto(
                any(Administrador.class), eq("Administrador creado correctamente")))
                .thenAnswer(invocation -> {
                    Administrador admin = invocation.getArgument(0);
                    return AdministradorResponseDTO.builder()
                            .id(admin.getId())
                            .name(admin.getNombre())
                            .apellidos(admin.getApellidos())
                            .email(admin.getEmail())
                            .rol(admin.getRol())
                            .activo(admin.isActivo())
                            .sede(admin.getSede())
                            .fechaIncorporacion(admin.getFechaIncorporacion())
                            .mensaje(invocation.getArgument(1))
                            .build();
                });

        var response = service.crearAdministrador(dto);

        assertEquals(201, response.getStatusCode().value());
        AdministradorResponseDTO body = response.getBody();
        assertEquals("ana@esibuy.com", body.getEmail());
        assertEquals(Rol.ADMINISTRADOR, body.getRol());
        assertEquals("Madrid", body.getSede());
        assertEquals("Administrador creado correctamente", body.getMensaje());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertInstanceOf(Administrador.class, saved);
        assertEquals(Rol.ADMINISTRADOR, saved.getRol());
        assertTrue(new BCryptPasswordEncoder().matches("Clave#2026x", saved.getContrasena()));
    }

    @Test
    void crearAdministrador_emailExistenteLanzaConflictoYSinGuardar() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(new Administrador()));

        assertThrows(ConflictException.class, () -> service.crearAdministrador(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearAdministrador_contrasenaQueNoCumplePoliticaNoGuarda() {
        AdministradorRegistroDTO dto = administradorDTO();
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Contraseña débil"))
                .when(passwordValidatorService).validatePassword(dto.contrasena(), List.of());

        assertThrows(ValidationException.class, () -> service.crearAdministrador(dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private AdministradorRegistroDTO administradorDTO() {
        return new AdministradorRegistroDTO(
                "Ana", "Pérez", "ana@esibuy.com", "Clave#2026x", "Madrid");
    }

    @Test
    void modificarUsuario_NoExiste() {
        String id = "usuario1";
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        UserPatchDTO dto = new UserPatchDTO(null, null, null, null, null,
            null, null, null, null, null, null);

        assertThrows(NotFoundException.class,() -> service.modificarUsuario(id, dto));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void modificarUsuario_administrador() {
        String id = "admin1";

        Administrador administrador = Administrador.builder()
            .nombre("Ana")
            .apellidos("Pérez")
            .email("ana@esibuy.com")
            .contrasena("password")
            .sede("Madrid")
            .build();

        UserPatchDTO dto = new UserPatchDTO("Laura", "Gómez", "600123456", "foto.jpg", 
            null, null, null, null, null, null, "Ciudad Real");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(administrador));

        service.modificarUsuario(id, dto);

        assertEquals("Laura", administrador.getNombre());
        assertEquals("Gómez", administrador.getApellidos());
        assertEquals("600123456", administrador.getTelefono());
        assertEquals("foto.jpg", administrador.getImagenPerfil());
        assertEquals("Ciudad Real", administrador.getSede());

        verify(usuarioRepository).save(administrador);
    }

    @Test
    void modificarUsuario_clienteMenorDeEdad() {
        String id = "cliente1";

        Cliente cliente = Cliente.builder()
            .nombre("Carlos")
            .apellidos("López")
            .email("carlos@esibuy.com")
            .contrasena("password")
            .dni("12345678A")
            .fechaNacimiento(LocalDate.of(2000, 1, 1))
            .tipoCliente(TipoCliente.NORMAL)
            .build();

        UserPatchDTO dto = new UserPatchDTO(
            null, null, null, null,
            null, LocalDate.now().minusYears(17),
            null, null, null, null, null);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(cliente));
        assertThrows(ResponseStatusException.class, () -> service.modificarUsuario(id, dto));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
