package esi.grupo5.esiBuy.Controller;

import esi.grupo5.esiBuy.Dto.MfaBackupCodesDTO;
import esi.grupo5.esiBuy.Dto.MfaConfigRequestDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupConfirmDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Exception.BusinessException;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Service.AuthFactorService;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthFactorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthFactorControllerTest {

    @Autowired private AuthFactorController controller;
    @MockitoBean private AuthService authService;
    @MockitoBean private AuthFactorService authFactorService;
    @MockitoBean private CookieUtil cookieUtil;
    @MockitoBean private JwtService jwtService;

    @Test
    void verifyMfa_soloEscribeCookieCuandoHayAccessToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        MfaVerifyRequestDTO request = new MfaVerifyRequestDTO("user@test.com", "123456", null);
        LoginResponseDTO withToken = new LoginResponseDTO("access", "refresh", "CLIENTE", "NORMAL");
        LoginResponseDTO withoutToken = new LoginResponseDTO(null, null, "CLIENTE", "NORMAL");
        when(authService.verifyMFA(request)).thenReturn(withToken).thenReturn(withoutToken);

        assertEquals(withToken, controller.verifyMFA(request, response).getBody());
        assertEquals(withoutToken, controller.verifyMFA(request, response).getBody());

        verify(cookieUtil).setTokenCookies(response, withToken);
        verify(cookieUtil, never()).setTokenCookies(response, withoutToken);
    }

    @Test
    void setupInit_validaEmailYDelegaLaInicializacion() {
        MfaSetupResponseDTO setup = new MfaSetupResponseDTO("qr-data", "secret");
        when(authFactorService.initializeSetup("user@test.com", "setup-token")).thenReturn(setup);

        assertEquals(setup, controller.initMfaSetup(Map.of(
                "email", "user@test.com", "setupToken", "setup-token")).getBody());
        assertThrows(ValidationException.class,
                () -> controller.initMfaSetup(Map.of("email", " ", "setupToken", "setup-token")));
        verify(authFactorService).initializeSetup("user@test.com", "setup-token");
    }

    @Test
    void setupConfirmYConfigDeleganLosDatosRecibidos() {
        MfaSetupConfirmDTO confirmation = new MfaSetupConfirmDTO(
                "user@test.com", "123456", "setup-token");
        MfaBackupCodesDTO backupCodes = new MfaBackupCodesDTO(List.of("12345678"), "configured");
        when(authFactorService.confirmSetup(confirmation)).thenReturn(backupCodes);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user-1");

        assertEquals(backupCodes, controller.confirmMfaSetup(confirmation).getBody());
        controller.configureMfa(new MfaConfigRequestDTO(true, false), authentication);

        verify(authFactorService).confirmSetup(confirmation);
        verify(authFactorService).configureMfa("user-1", true, false);
    }

    @Test
    void verifyMfa_traduceErroresDeNegocioConservandoElCodigo() {
        MfaVerifyRequestDTO request = new MfaVerifyRequestDTO("user@test.com", "123456", null);
        when(authService.verifyMFA(any())).thenThrow(
                new BusinessException("verification failed", 403, "MFA_INVALID"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> controller.verifyMFA(request, mock(HttpServletResponse.class)));

        assertEquals(403, error.getHttpStatusCode());
        assertEquals("MFA_INVALID", error.getErrorCode());
    }
}
