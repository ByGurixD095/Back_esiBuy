package esi.grupo5.esiBuy.Controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import esi.grupo5.esiBuy.Dto.*;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Service.AuthFactorService;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

/** Expone los endpoints de configuración y verificación de autenticación multifactor. */
@RestController
@RequestMapping("/mfa")
public class AuthFactorController {

    /** Servicio de autenticación que genera la respuesta final del inicio de sesión. */
    private final AuthService authService;
    /** Servicio de preparación y verificación de los factores MFA. */
    private final AuthFactorService authFactorService;
    /** Utilidad para establecer cookies de sesión. */
    private final CookieUtil cookieUtil;

    /**
     * Crea el controlador MFA.
     *
     * @param authService servicio de autenticación
     * @param authFactorService servicio de factores
     * @param cookieUtil utilidad para las cookies
     */
    public AuthFactorController(AuthService authService, AuthFactorService authFactorService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.authFactorService = authFactorService;
        this.cookieUtil = cookieUtil;
    }

    /**
     * Verifica los factores enviados y establece cookies si se completó la autenticación.
     *
     * @param dto correo y códigos MFA que se validarán
     * @param response respuesta donde se incluirán las cookies si corresponde
     * @return resultado de autenticación con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.AuthException si falla el código TOTP
     * @throws esi.grupo5.esiBuy.Exception.ExpiredTokenException si el OTP de correo no es válido
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponseDTO> verifyMFA(@Valid @RequestBody MfaVerifyRequestDTO dto, HttpServletResponse response) {
        LoginResponseDTO loginResponse = authService.verifyMFA(dto);
        cookieUtil.setTokenCookiesIfPresent(response, loginResponse);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Inicia la configuración MFA con las claves {@code email} y {@code setupToken}
     * proporcionadas en el cuerpo.
     *
     * @param request mapa con correo y token temporal
     * @return secreto y URI de imagen QR con estado HTTP 200
     * @throws ValidationException si falta el correo
     */
    @PostMapping("/setup-init")
    public ResponseEntity<MfaSetupResponseDTO> initMfaSetup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String setupToken = request.get("setupToken");
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
        return ResponseEntity.ok(authFactorService.initializeSetup(email, setupToken));
    }

    /**
     * Confirma la configuración MFA con el código TOTP indicado.
     *
     * @param dto correo, desafío de configuración y código de confirmación
     * @return códigos de respaldo generados
     */
    @PostMapping("/setup-confirm")
    public ResponseEntity<MfaBackupCodesDTO> confirmMfaSetup(@Valid @RequestBody MfaSetupConfirmDTO dto) {
        return ResponseEntity.ok(authFactorService.confirmSetup(dto));
    }

    /**
     * Actualiza las opciones MFA del usuario autenticado.
     *
     * @param dto indicadores para activar segundo y tercer factor
     * @param authentication identidad autenticada de la petición
     * @return respuesta vacía con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si la cuenta no existe o está eliminada
     */
    @PostMapping("/config")
    public ResponseEntity<Void> configureMfa(@RequestBody MfaConfigRequestDTO dto,
                                             Authentication authentication) {
        authFactorService.configureMfa((String) authentication.getPrincipal(), dto.enable2fa(), dto.enable3fa());
        return ResponseEntity.ok().build();
    }
}
