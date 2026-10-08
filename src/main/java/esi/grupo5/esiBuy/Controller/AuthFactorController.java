package esi.grupo5.esiBuy.Controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import esi.grupo5.esiBuy.Dto.*;
import esi.grupo5.esiBuy.Exception.*;
import esi.grupo5.esiBuy.Service.AuthFactorService;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/mfa")
public class AuthFactorController {

    private final AuthService authService;
    private final AuthFactorService authFactorService;
    private final CookieUtil cookieUtil;

    public AuthFactorController(AuthService authService, AuthFactorService authFactorService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.authFactorService = authFactorService;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponseDTO> verifyMFA(@Valid @RequestBody MfaVerifyRequestDTO dto, HttpServletResponse response) {
        try {
            LoginResponseDTO loginResponse = authService.verifyMFA(dto);
            if (loginResponse.accessToken() != null) {
                cookieUtil.setTokenCookies(response, loginResponse);
            }
            return ResponseEntity.ok(loginResponse);
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage(), e.getHttpStatusCode(), e.getErrorCode());
        }
    }

    @PostMapping("/setup-init")
    public ResponseEntity<MfaSetupResponseDTO> initMfaSetup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String setupToken = request.get("setupToken");
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email es requerido");
        }
        return ResponseEntity.ok(authFactorService.initializeSetup(email, setupToken));
    }

    @PostMapping("/setup-confirm")
    public ResponseEntity<MfaBackupCodesDTO> confirmMfaSetup(@Valid @RequestBody MfaSetupConfirmDTO dto) {
        return ResponseEntity.ok(authFactorService.confirmSetup(dto));
    }

    @PostMapping("/config")
    public ResponseEntity<Void> configureMfa(@RequestBody MfaConfigRequestDTO dto,
                                             Authentication authentication) {
        authFactorService.configureMfa((String) authentication.getPrincipal(), dto.enable2fa(), dto.enable3fa());
        return ResponseEntity.ok().build();
    }
}
