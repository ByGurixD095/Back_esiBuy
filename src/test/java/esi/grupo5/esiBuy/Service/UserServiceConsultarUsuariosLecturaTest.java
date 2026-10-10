package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.AdministradorResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteResponseDTO;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Dto.VendedorResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ConflictException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Model.Administrador;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceConsultarUsuariosLecturaTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordValidatorService passwordValidatorService;
    @Mock private AuthService authService;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(
                usuarioRepository,
                new BCryptPasswordEncoder(),
                passwordValidatorService,
                authService);
    }

    @Test
    void consultarUsuarios_existentesDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        cliente.setBloqueado(false);
        cliente.setFechaAlta(LocalDateTime.of(2025, 1, 2, 3, 4));
        cliente.setMfaConfigurado(true);
        cliente.setIs2faActivoCliente(true);
        cliente.setIs3faActivoCliente(true);
        Cliente usuarioEliminado = cliente("cliente-eliminado", "Luis", "luis@test.com");
        usuarioEliminado.setEliminado(true);
        when(usuarioRepository.findAll()).thenReturn(List.of(cliente, usuarioEliminado));

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        assertEquals(2, resultado.size());
        assertEquals("cliente-1", resultado.get(0).getId());
        assertEquals("Ana", resultado.get(0).getName());
        assertEquals("López", resultado.get(0).getApellidos());
        assertEquals("ana@test.com", resultado.get(0).getEmail());
        assertEquals(Rol.CLIENTE, resultado.get(0).getRol());
        assertEquals(true, resultado.get(0).isActivo());
        assertEquals(false, resultado.get(0).isBloqueado());
        assertEquals("12345678A", ((ClienteResponseDTO) resultado.get(0)).getDni());
        assertEquals(cliente.getFechaAlta(), resultado.get(0).getFechaAlta());
        assertEquals(cliente.getFechaCambioContrasena(), resultado.get(0).getFechaCambioContrasena());
        assertEquals(true, resultado.get(0).isMfaConfigurado());
        assertEquals(true, resultado.get(0).isDosFactorActivoCliente());
        assertEquals(true, resultado.get(0).isTresFactorActivoCliente());
        assertEquals("cliente-eliminado", resultado.get(1).getId());
        assertEquals(true, resultado.get(1).isEliminado());
        verify(usuarioRepository).findAll();
    }

    @Test
    void consultarUsuarios_vendedorIncluyeSusDatosEspecificos() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        vendedor.setNombre("Luis");
        vendedor.setNombreComercial("Tienda Luis");
        vendedor.setCifNif("B12345678");
        vendedor.setCategoriaPrincipalId("electronica");
        when(usuarioRepository.findAll()).thenReturn(List.of(vendedor));

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        VendedorResponseDTO dto = (VendedorResponseDTO) resultado.get(0);
        assertEquals("Tienda Luis", dto.getNombreComercial());
        assertEquals("B12345678", dto.getCifNif());
        assertEquals("electronica", dto.getCategoriaPrincipalId());
    }

    @Test
    void consultarUsuarios_administradorIncluyeDatosComunesYEspecificos() {
        Administrador administrador = Administrador.builder()
                .nombre("Marta")
                .apellidos("García")
                .email("marta@test.com")
                .contrasena("no-debe-exponerse")
                .sede("Madrid")
                .build();
        administrador.setId("admin-1");
        administrador.setMfaConfigurado(true);
        when(usuarioRepository.findAll()).thenReturn(List.of(administrador));

        AdministradorResponseDTO resultado =
                (AdministradorResponseDTO) service.getAllUsers().get(0);

        assertEquals("admin-1", resultado.getId());
        assertEquals(Rol.ADMINISTRADOR, resultado.getRol());
        assertEquals("Madrid", resultado.getSede());
        assertEquals(administrador.getFechaIncorporacion(), resultado.getFechaIncorporacion());
        assertEquals(true, resultado.isMfaConfigurado());
        assertEquals(true, resultado.isActivo());
    }

    @Test
    void consultarUsuarios_sinUsuariosDevuelveListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<UsuarioResponseDTO> resultado = service.getAllUsers();

        assertEquals(List.of(), resultado);
        verify(usuarioRepository).findAll();
    }

    @Test
    void consultarUsuarioPorId_existenteDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1"))
                .thenReturn(Optional.of(cliente));

        UsuarioResponseDTO resultado = service.getUserById("cliente-1");

        assertEquals("cliente-1", resultado.getId());
        assertEquals("Ana", resultado.getName());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals(true, resultado.isActivo());
        assertEquals(TipoCliente.NORMAL, ((ClienteResponseDTO) resultado).getTipoCliente());
        verify(usuarioRepository).findByIdAndEliminadoFalse("cliente-1");
    }

    @Test
    void consultarUsuarioPorId_inexistenteOLoEliminadoLanzaNotFound() {
        when(usuarioRepository.findByIdAndEliminadoFalse("no-existe"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUserById("no-existe"));

        verify(usuarioRepository).findByIdAndEliminadoFalse("no-existe");
    }

    @Test
    void registrarCliente_validaGuardaYCompletaAutenticacion() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", "123456789",
                null, "12345678A", LocalDate.of(2000, 1, 1), null);
        LoginResponseDTO respuesta = new LoginResponseDTO("access", "refresh", "CLIENTE", "NORMAL");
        doReturn(false).when(usuarioRepository).existsByEmail(dto.email());
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authService.completarAutenticacion(any())).thenReturn(respuesta);

        LoginResponseDTO resultado = service.registrarCliente(dto);

        assertEquals(respuesta, resultado);
        verify(passwordValidatorService).validatePassword(eq(dto.contrasena()), any());
        verify(usuarioRepository).save(any(Cliente.class));
        verify(authService).completarAutenticacion(any());
    }

    @Test
    void registrarVendedor_emailExistenteImpideGuardar() {
        VendedorRegisterRequest dto = new VendedorRegisterRequest(
                "Luis", "García", "luis@test.com", "Strong#2026", null, null,
                "Tienda Luis", "B12345678", "electronica");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.registrarVendedor(dto));

        verify(passwordValidatorService).validatePassword(eq(dto.contrasena()), any());
        verify(usuarioRepository, never()).save(any());
        verify(authService, never()).completarAutenticacion(any());
    }

    @Test
    void registrarCliente_erroresDeRepositorioSeTraducenAErrorDeNegocio() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", null, null,
                "12345678A", LocalDate.of(2000, 1, 1), TipoCliente.NORMAL);
        when(usuarioRepository.existsByEmail(dto.email())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(BusinessException.class, () -> service.registrarCliente(dto));

        doReturn(false).when(usuarioRepository).existsByEmail(dto.email());
        doThrow(new IllegalStateException("database unavailable"))
                .when(usuarioRepository).save(any(Cliente.class));
        assertThrows(BusinessException.class, () -> service.registrarCliente(dto));
    }

    @Test
    void registrarCliente_errorDeValidacionDePasswordSeTraduce() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "weak", null, null,
                "12345678A", LocalDate.of(2000, 1, 1), TipoCliente.NORMAL);
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "password policy"))
                .when(passwordValidatorService).validatePassword(eq(dto.contrasena()), any());

        assertThrows(esi.grupo5.esiBuy.Exception.ValidationException.class,
                () -> service.registrarCliente(dto));

        verify(usuarioRepository, never()).existsByEmail(any());
    }

    @Test
    void modificarPerfil_actualizaCamposValidosYTraduceErroresDeGuardado() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1")).thenReturn(Optional.of(cliente));

        service.modificarMiPerfil("cliente-1", new UserSelfUpdateDTO(
                "Eva", "Gómez", "987654321", "avatar.png", TipoCliente.PREMIUM, null, null));

        assertEquals("Eva", cliente.getNombre());
        assertEquals("Gómez", cliente.getApellidos());
        assertEquals("987654321", cliente.getTelefono());
        assertEquals("avatar.png", cliente.getImagenPerfil());
        assertEquals(TipoCliente.PREMIUM, cliente.getTipoCliente());
        verify(usuarioRepository).save(cliente);

        doThrow(new org.springframework.dao.DuplicateKeyException("duplicate"))
                .when(usuarioRepository).save(cliente);
        assertThrows(ConflictException.class, () -> service.modificarMiPerfil(
                "cliente-1", new UserSelfUpdateDTO(null, null, null, null, null, null, null)));
        doThrow(new IllegalStateException("database unavailable"))
                .when(usuarioRepository).save(cliente);
        assertThrows(BusinessException.class, () -> service.modificarMiPerfil(
                "cliente-1", new UserSelfUpdateDTO(null, null, null, null, null, null, null)));
    }

    @Test
    void modificarPerfilVendedor_actualizaDatosComercialesYUsuarioInexistenteFalla() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        when(usuarioRepository.findByIdAndEliminadoFalse("vendedor-1")).thenReturn(Optional.of(vendedor));

        service.modificarMiPerfil("vendedor-1", new UserSelfUpdateDTO(
                "Luis", null, null, null, null, "tecnologia", "Tienda Luis"));

        assertEquals("Luis", vendedor.getNombre());
        assertEquals("tecnologia", vendedor.getCategoriaPrincipalId());
        assertEquals("Tienda Luis", vendedor.getNombreComercial());
        verify(usuarioRepository).save(vendedor);

        when(usuarioRepository.findByIdAndEliminadoFalse("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.modificarMiPerfil(
                "missing", new UserSelfUpdateDTO(null, null, null, null, null, null, null)));
    }

    private Cliente cliente(String id, String nombre, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellidos("López");
        cliente.setEmail(email);
        cliente.setRol(Rol.CLIENTE);
        cliente.setDni("12345678A");
        return cliente;
    }
}
