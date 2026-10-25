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
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
  import static org.junit.jupiter.api.Assertions.assertFalse;
  import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
        when(usuarioRepository.findAll()).thenReturn(List.of(cliente, usuarioEliminado));

        List<UsuarioResponseDTO> resultado = service.getAllUsers(null, null, null);

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

        List<UsuarioResponseDTO> resultado = service.getAllUsers(null, null, null);

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
                (AdministradorResponseDTO) service.getAllUsers(null, null, null).get(0);

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

        List<UsuarioResponseDTO> resultado = service.getAllUsers(null, null, null);

        assertEquals(List.of(), resultado);
        verify(usuarioRepository).findAll();
    }

    @Test
    void consultarUsuarioPorId_existenteDevuelveSusDatosPublicos() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setActivo(true);
        when(usuarioRepository.findById("cliente-1"))
                .thenReturn(Optional.of(cliente));

        UsuarioResponseDTO resultado = service.getUserById("cliente-1");

        assertEquals("cliente-1", resultado.getId());
        assertEquals("Ana", resultado.getName());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals(true, resultado.isActivo());
        assertEquals(TipoCliente.NORMAL, ((ClienteResponseDTO) resultado).getTipoCliente());
        verify(usuarioRepository).findById("cliente-1");
    }

    @Test
    void consultarUsuarioPorId_inexistenteOLoEliminadoLanzaNotFound() {
        when(usuarioRepository.findById("no-existe"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUserById("no-existe"));

        verify(usuarioRepository).findById("no-existe");
    }
    
    @Test
    void consultarUsuarioAdminPorId_permiteConsultarUsuarioEliminado() {
        Cliente cliente = cliente("cliente-eliminado", "Ana", "ana@test.com");
        when(usuarioRepository.findById("cliente-eliminado"))
                .thenReturn(Optional.of(cliente));

        UsuarioResponseDTO resultado = service.getAdminUserById("cliente-eliminado");

        assertEquals("cliente-eliminado", resultado.getId());
        verify(usuarioRepository).findById("cliente-eliminado");
    }

    @Test
    void registrarCliente_guardaInactivoYPideActivacionSinAutenticar() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", "Strong#2026", "123456789",
                null, "12345678A", LocalDate.of(2000, 1, 1), null);
        doReturn(false).when(usuarioRepository).existsByEmail(dto.email());
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponseDTO resultado = service.registrarCliente(dto);

        assertEquals("PENDING_ACTIVATION", resultado.mfaStatus());
        assertEquals("ana@test.com", resultado.email());
        assertNull(resultado.accessToken());
        assertNull(resultado.refreshToken());
        verify(passwordValidatorService).validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email(), null);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(usuarioRepository).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        verify(authService, never()).completarAutenticacion(any());
    }

    @Test
    void registrarVendedor_guardaInactivoYPideActivacionSinAutenticar() {
        VendedorRegisterRequest dto = new VendedorRegisterRequest(
                "Luis", "García", "luis@test.com", "Strong#2026", "Strong#2026", null, null,
                "Tienda Luis", "B12345678", "electronica");
        doReturn(false).when(usuarioRepository).existsByEmail(dto.email());
        when(usuarioRepository.save(any(Vendedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponseDTO resultado = service.registrarVendedor(dto);

        assertEquals("PENDING_ACTIVATION", resultado.mfaStatus());
        assertNull(resultado.accessToken());

        ArgumentCaptor<Vendedor> captor = ArgumentCaptor.forClass(Vendedor.class);
        verify(usuarioRepository).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        verify(authService, never()).completarAutenticacion(any());
    }

    @Test
    void registrarVendedor_emailExistenteImpideGuardar() {
         VendedorRegisterRequest dto = new VendedorRegisterRequest(
                        "Luis", "García", "luis@test.com", "Strong#2026", "Strong#2026", null, null,
                        "Tienda Luis", "B12345678", "electronica");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.registrarVendedor(dto));

        verify(passwordValidatorService).validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email(), null);
        verify(usuarioRepository, never()).save(any());
        verify(authService, never()).completarAutenticacion(any());
    }

    @Test
    void registrarVendedor_nombreComercialExistenteImpideGuardar() {
        VendedorRegisterRequest dto = new VendedorRegisterRequest(
                "Luis", "García", "luis@test.com", "Strong#2026", "Strong#2026", null, null,
                "Tienda Luis", "B12345678", "electronica");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.existsByNombreComercial(dto.nombreComercial())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class, () -> service.registrarVendedor(dto));

        assertEquals("El nombre comercial ya está en uso", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(authService, never()).completarAutenticacion(any());
    }

    @Test
    void registrarCliente_erroresDeRepositorioSeTraducenAErrorDeNegocio() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "Strong#2026", "Strong#2026", null, null,
                "12345678A", LocalDate.of(2000, 1, 1), TipoCliente.NORMAL);
        when(usuarioRepository.existsByEmail(dto.email())).thenThrow(
                new org.springframework.dao.DataAccessResourceFailureException("database unavailable"));

        assertThrows(BusinessException.class, () -> service.registrarCliente(dto));

        doReturn(false).when(usuarioRepository).existsByEmail(dto.email());
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("database unavailable"))
                .when(usuarioRepository).save(any(Cliente.class));
        assertThrows(BusinessException.class, () -> service.registrarCliente(dto));
    }

    @Test
    void registrarCliente_errorDeValidacionDePasswordSeTraduce() {
        ClienteRegistroDTO dto = new ClienteRegistroDTO(
                "Ana", "López", "ana@test.com", "weak", "weak", null, null,
                "12345678A", LocalDate.of(2000, 1, 1), TipoCliente.NORMAL);
        doThrow(new esi.grupo5.esiBuy.Exception.ValidationException("password policy"))
                .when(passwordValidatorService).validatePassword(dto.contrasena(), dto.confirmarContrasena(), dto.nombre(), dto.email(), null);

        assertThrows(esi.grupo5.esiBuy.Exception.ValidationException.class,
                () -> service.registrarCliente(dto));

        verify(usuarioRepository, never()).existsByEmail(any());
    }

    @Test
    void modificarPerfil_actualizaCamposValidosYTraduceErroresDeGuardado() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        service.modificarMiPerfil("cliente-1", new UserSelfUpdateDTO(
                "Eva", "Gómez", "987654321", "avatar.png", TipoCliente.PREMIUM, null, null, null));

        assertEquals("Eva", cliente.getNombre());
        assertEquals("Gómez", cliente.getApellidos());
        assertEquals("987654321", cliente.getTelefono());
        assertEquals("avatar.png", cliente.getImagenPerfil());
        assertEquals(TipoCliente.PREMIUM, cliente.getTipoCliente());
        verify(usuarioRepository).save(cliente);

        doThrow(new org.springframework.dao.DuplicateKeyException("duplicate"))
                .when(usuarioRepository).save(cliente);
        assertThrows(ConflictException.class, () -> service.modificarMiPerfil(
                "cliente-1", new UserSelfUpdateDTO(null, null, null, null, null, null, null, null)));
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("database unavailable"))
                .when(usuarioRepository).save(cliente);
        assertThrows(BusinessException.class, () -> service.modificarMiPerfil(
                "cliente-1", new UserSelfUpdateDTO(null, null, null, null, null, null, null, null)));
    }

    @Test
    void modificarPerfilVendedor_actualizaDatosComercialesYUsuarioInexistenteFalla() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));

        service.modificarMiPerfil("vendedor-1", new UserSelfUpdateDTO(
                "Luis", null, null, null, null, "tecnologia", "Tienda Luis", null));

        assertEquals("Luis", vendedor.getNombre());
        assertEquals("tecnologia", vendedor.getCategoriaPrincipalId());
        assertEquals("Tienda Luis", vendedor.getNombreComercial());
        verify(usuarioRepository).save(vendedor);

        when(usuarioRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.modificarMiPerfil(
                "missing", new UserSelfUpdateDTO(null, null, null, null, null, null, null, null)));
    }

    @Test
    void modificarPerfilAdministrador_actualizaSede() {
        Administrador administrador = Administrador.builder()
                .nombre("Ana")
                .apellidos("Pérez")
                .email("ana-admin@test.com")
                .contrasena("password")
                .sede("Madrid")
                .build();
        administrador.setId("admin-1");
        when(usuarioRepository.findById("admin-1"))
                .thenReturn(Optional.of(administrador));

        service.modificarMiPerfil("admin-1", new UserSelfUpdateDTO(
                null, null, null, null, null, null, null, "Sevilla"));

        assertEquals("Sevilla", administrador.getSede());
        verify(usuarioRepository).save(administrador);
    }

    @Test
    void modificarPerfilVendedor_nombreComercialDuplicadoImpideLaActualizacion() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId("vendedor-1");
        vendedor.setNombreComercial("Tienda original");
        when(usuarioRepository.findById("vendedor-1")).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.existsByNombreComercialAndIdNot("Tienda ocupada", "vendedor-1"))
                .thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> service.modificarMiPerfil(
                "vendedor-1",
                new UserSelfUpdateDTO(null, null, null, null, null, null, "Tienda ocupada", null)));

        assertEquals("El nombre comercial ya está en uso", exception.getMessage());
        assertEquals("Tienda original", vendedor.getNombreComercial());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void modificarPerfil_ignoraCamposComunesEnBlanco() {
        Cliente cliente = cliente("cliente-1", "Ana", "ana@test.com");
        cliente.setApellidos("López");
        cliente.setTelefono("123456789");
        cliente.setImagenPerfil("avatar.png");
        when(usuarioRepository.findById("cliente-1"))
                .thenReturn(Optional.of(cliente));

        service.modificarMiPerfil("cliente-1", new UserSelfUpdateDTO(
                " ", "", " ", " ", null, null, null, null));

        assertEquals("Ana", cliente.getNombre());
        assertEquals("López", cliente.getApellidos());
        assertEquals("123456789", cliente.getTelefono());
        assertEquals("avatar.png", cliente.getImagenPerfil());
        verify(usuarioRepository).save(cliente);
    }

    private Cliente cliente(String id, String nombre, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellidos("López");
        cliente.setEmail(email);
        cliente.setDni("12345678A");
        return cliente;
    }
}
