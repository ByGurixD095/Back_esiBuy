package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupResponseDTO;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Model.enums.TipoCliente;
import esi.grupo5.esiBuy.Repository.RefreshTokenRepository;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFactorServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthFactorService authFactorService;

    @BeforeEach
    void setUp() {
        // Inyectamos la clave de encriptación que normalmente lee de application.properties
        ReflectionTestUtils.setField(authFactorService, "encryptionKey", "1234567890123456");
    }

    @Test
    void requiereMfaObligatorio_Vendedor_ReturnsTrue() {
        Vendedor vendedor = new Vendedor();
        vendedor.setRol(Rol.VENDEDOR);

        boolean requiereMfa = authFactorService.requiereMfaObligatorio(vendedor);

        assertTrue(requiereMfa, "Un vendedor siempre debe requerir MFA obligatorio");
    }

    @Test
    void requiereMfaObligatorio_ClienteSin2fa_ReturnsFalse() {
        Cliente cliente = new Cliente();
        cliente.setRol(Rol.CLIENTE);
        cliente.setIs2faActivoCliente(false);

        boolean requiereMfa = authFactorService.requiereMfaObligatorio(cliente);

        assertFalse(requiereMfa, "Un cliente con el 2FA desactivado no requiere MFA");
    }

    @Test
    void completarAutenticacion_VendedorSinMfaConfigurado_ReturnsRequiresSetup() {
        Vendedor vendedor = new Vendedor();
        vendedor.setRol(Rol.VENDEDOR);
        vendedor.setEmail("vendedor@test.com");
        vendedor.setMfaConfigurado(false); // No ha configurado el Authenticator

        LoginResponseDTO response = authFactorService.completarAutenticacion(vendedor);

        assertEquals("REQUIRES_MFA_SETUP", response.mfaStatus());
        assertNull(response.accessToken());
    }

    @Test
    void completarAutenticacion_VendedorConMfaConfigurado_ReturnsRequiresVerification() {
        Vendedor vendedor = new Vendedor();
        vendedor.setRol(Rol.VENDEDOR);
        vendedor.setEmail("vendedor@test.com");
        vendedor.setMfaConfigurado(true); // Ya tiene el Authenticator

        LoginResponseDTO response = authFactorService.completarAutenticacion(vendedor);

        assertEquals("REQUIRES_MFA_VERIFICATION", response.mfaStatus());
        assertNull(response.accessToken());
        // Verificamos que se guarde el usuario al generar el código Email OTP
        verify(usuarioRepository, times(1)).save(vendedor);
    }

    @Test
    void initMfaSetup_UsuarioExiste_GeneraSecretoYQr() {
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@test.com");
        
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));

        MfaSetupResponseDTO response = authFactorService.initMfaSetup("cliente@test.com");

        assertNotNull(response.manualSecretKey());
        assertNotNull(response.qrCodeUri());
        assertTrue(response.qrCodeUri().startsWith("data:image/png;base64,"));
        
        // Verificamos que el secreto se haya cifrado y guardado en la base de datos
        assertNotNull(cliente.getTotpSecretCifrado());
        verify(usuarioRepository, times(1)).save(cliente);
    }
}