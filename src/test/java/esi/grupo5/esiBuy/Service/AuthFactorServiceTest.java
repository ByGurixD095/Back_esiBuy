package esi.grupo5.esiBuy.Service;

import esi.grupo5.esiBuy.Dto.MfaSetupConfirmDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Exception.AuthException;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ExpiredTokenException;
import esi.grupo5.esiBuy.Exception.NotFoundException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Model.Cliente;
import esi.grupo5.esiBuy.Model.Vendedor;
import esi.grupo5.esiBuy.Model.enums.Rol;
import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFactorServiceTest {
    @Mock private EmailService emailService;
    @Mock private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthFactorService service;

    @BeforeEach
    void setUp() {
        service = new AuthFactorService(emailService, encoder, usuarioRepository);
        ReflectionTestUtils.setField(service, "encryptionKey", "1234567890123456");
    }

    @Test
    void vendedor_requiereMfa() {
        Vendedor usuario = new Vendedor();
        usuario.setRol(Rol.VENDEDOR);
        assertTrue(service.requiereMfaObligatorio(usuario));
    }

    @Test
    void clienteSinFactores_noRequiereMfa() {
        Cliente usuario = new Cliente();
        usuario.setRol(Rol.CLIENTE);
        assertFalse(service.requiereMfaObligatorio(usuario));
    }

    @Test
    void setupChallenge_seGuardaHasheadoYSePuedeValidar() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("vendedor@test.com");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String token = service.createSetupChallenge(usuario);

        assertNotNull(token);
        assertNotEquals(token, usuario.getMfaSetupTokenHash());
        assertNotNull(usuario.getMfaSetupTokenExpiracion());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertDoesNotThrow(() -> service.requireSetupAccess(usuario.getEmail(), token));
    }

    @Test
    void setupChallenge_invalido_esRechazado() {
        Vendedor usuario = new Vendedor();
        usuario.setEmail("vendedor@test.com");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertThrows(RuntimeException.class,
                () -> service.requireSetupAccess(usuario.getEmail(), "invalid"));
    }

    @Test
    void requireSetupAccess_rechazaUsuarioInexistenteYChallengeExpirado() {
        when(usuarioRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.requireSetupAccess("missing@test.com", "token"));

        Vendedor usuario = vendedor("vendedor@test.com");
        usuario.setMfaSetupTokenHash("hash");
        usuario.setMfaSetupTokenExpiracion(LocalDateTime.now().minusSeconds(1));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertThrows(ExpiredTokenException.class,
                () -> service.requireSetupAccess(usuario.getEmail(), "token"));
    }

    @Test
    void initializeSetup_generaSecretoYUriQr() {
        Vendedor usuario = vendedor("vendedor@test.com");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String token = service.createSetupChallenge(usuario);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var response = service.initializeSetup(usuario.getEmail(), token);

        assertTrue(response.qrCodeUri().startsWith("data:image/png;base64,"));
        assertFalse(response.manualSecretKey().isBlank());
        assertNotNull(usuario.getTotpSecretCifrado());
        verify(usuarioRepository, times(2)).save(usuario);
    }

    @Test
    void confirmSetup_activaMfaYDevuelveCodigosDeRespaldo() {
        Vendedor usuario = vendedor("vendedor@test.com");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String token = service.createSetupChallenge(usuario);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        AuthFactorService serviceSpy = spy(service);
        doReturn(true).when(serviceSpy).verificarCodigoTotp(usuario, "123456");

        var result = serviceSpy.confirmSetup(new MfaSetupConfirmDTO(
                usuario.getEmail(), "123456", token));

        assertTrue(usuario.isMfaConfigurado());
        assertNull(usuario.getMfaSetupTokenHash());
        assertNull(usuario.getMfaSetupTokenExpiracion());
        assertEquals(8, result.backupCodes().size());
        assertEquals(8, usuario.getCodigosRespaldoHasheados().size());
        assertTrue(result.backupCodes().stream().allMatch(code -> code.matches("\\d{8}")));
    }

    @Test
    void confirmSetup_rechazaCodigoTotpIncorrecto() {
        Vendedor usuario = vendedor("vendedor@test.com");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String token = service.createSetupChallenge(usuario);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(ValidationException.class, () -> service.confirmSetup(
                new MfaSetupConfirmDTO(usuario.getEmail(), "123456", token)));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void configureMfa_actualizaAmbosFactoresYRechazaUsuarioInexistente() {
        Cliente cliente = new Cliente();
        when(usuarioRepository.findByIdAndEliminadoFalse("cliente-1")).thenReturn(Optional.of(cliente));

        service.configureMfa("cliente-1", true, true);

        assertTrue(cliente.is2faActivoCliente());
        assertTrue(cliente.is3faActivoCliente());
        verify(usuarioRepository).save(cliente);

        when(usuarioRepository.findByIdAndEliminadoFalse("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.configureMfa("missing", false, false));
    }

    @Test
    void backupCode_validoSeConsumeUnaSolaVez() {
        Vendedor usuario = vendedor("vendedor@test.com");
        usuario.setCodigosRespaldoHasheados(new ArrayList<>(List.of(encoder.encode("87654321"))));

        assertTrue(service.verificarCodigoRespaldo(usuario, "87654321"));
        assertTrue(usuario.getCodigosRespaldoHasheados().isEmpty());
        assertFalse(service.verificarCodigoRespaldo(usuario, "87654321"));
    }

    @Test
    void otpEmail_seGuardaSeEnviaYSeValidaConExpiracion() throws Exception {
        Vendedor usuario = vendedor("vendedor@test.com");
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);

        service.prepareVerification(usuario);

        verify(emailService).sendHtmlEmail(eq(usuario.getEmail()), eq("Código de acceso esiBuy"), html.capture());
        verify(usuarioRepository).save(usuario);
        Matcher matcher = Pattern.compile("<strong>(\\d{6})</strong>").matcher(html.getValue());
        assertTrue(matcher.find());
        assertTrue(service.verificarEmailOtp(usuario, matcher.group(1)));
        assertFalse(service.verificarEmailOtp(usuario, "000000"));

        usuario.setEmailOtpExpiracion(LocalDateTime.now().minusSeconds(1));
        assertFalse(service.verificarEmailOtp(usuario, matcher.group(1)));
        usuario.setEmailOtpExpiracion(null);
        assertFalse(service.verificarEmailOtp(usuario, matcher.group(1)));
    }

    @Test
    void prepareVerification_errorAlEnviarEmailSePropagaComoBusinessException() throws Exception {
        Vendedor usuario = vendedor("vendedor@test.com");
        doThrow(new MessagingException("mail unavailable"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        assertThrows(BusinessException.class, () -> service.prepareVerification(usuario));
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void verifyFactors_validaTotpYOtpDeEmailYLimpiaElOtp() {
        Vendedor usuario = vendedor("vendedor@test.com");
        usuario.setEmailOtpHash(encoder.encode("654321"));
        usuario.setEmailOtpExpiracion(LocalDateTime.now().plusMinutes(5));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        AuthFactorService serviceSpy = spy(service);
        doReturn(true).when(serviceSpy).verificarCodigoTotp(usuario, "123456");

        assertSame(usuario, serviceSpy.verifyFactors(
                new MfaVerifyRequestDTO(usuario.getEmail(), "123456", "654321")));

        assertNull(usuario.getEmailOtpHash());
        assertNull(usuario.getEmailOtpExpiracion());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void verifyFactors_rechazaCodigoTOTPInvalidoYOtpExpirado() {
        Vendedor usuario = vendedor("vendedor@test.com");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertThrows(AuthException.class, () -> service.verifyFactors(
                new MfaVerifyRequestDTO(usuario.getEmail(), "bad", null)));

        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@test.com");
        cliente.setIs3faActivoCliente(true);
        when(usuarioRepository.findByEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));
        AuthFactorService serviceSpy = spy(service);
        doReturn(true).when(serviceSpy).verificarCodigoTotp(cliente, "123456");
        cliente.setEmailOtpHash(encoder.encode("654321"));
        cliente.setEmailOtpExpiracion(LocalDateTime.now().minusSeconds(1));
        assertThrows(ExpiredTokenException.class, () -> serviceSpy.verifyFactors(
                new MfaVerifyRequestDTO(cliente.getEmail(), "123456", "654321")));
    }

    @Test
    void totpSecret_cifraYPermiteVerificarUnCodigoInvalidoSinExponerElSecreto() {
        String encrypted = service.cifrar("secret");
        assertNotEquals("secret", encrypted);

        Vendedor usuario = vendedor("vendedor@test.com");
        usuario.setTotpSecretCifrado(encrypted);
        assertFalse(service.verificarCodigoTotp(usuario, "invalid"));
        assertFalse(service.verificarCodigoTotp(vendedor("empty@test.com"), "123456"));
    }

    @Test
    void requiereMfaObligatorio_consideraElSegundoFactorDeCliente() {
        Cliente cliente = new Cliente();
        cliente.setRol(Rol.CLIENTE);
        cliente.setIs2faActivoCliente(true);

        assertTrue(service.requiereMfaObligatorio(cliente));
    }

    private Vendedor vendedor(String email) {
        Vendedor vendedor = new Vendedor();
        vendedor.setEmail(email);
        vendedor.setRol(Rol.VENDEDOR);
        return vendedor;
    }
}
