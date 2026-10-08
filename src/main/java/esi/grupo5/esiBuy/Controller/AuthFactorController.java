package esi.grupo5.esiBuy.Controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import esi.grupo5.esiBuy.Dto.*;
import esi.grupo5.esiBuy.Service.AuthFactorService;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Util.CookieUtil;

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
        LoginResponseDTO loginResponse = authService.verifyMFA(dto);
        if (loginResponse.accessToken() != null) {
            cookieUtil.setTokenCookies(response, loginResponse); // Usamos la utilidad centralizada
        }
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/setup-init")
    public ResponseEntity<MfaSetupResponseDTO> initMfaSetup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String setupToken = request.get("setupToken");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email es requerido");
        }
        return ResponseEntity.ok(authFactorService.initializeSetup(email, setupToken));
    }

    @PostMapping("/setup-confirm")
    public ResponseEntity<MfaBackupCodesDTO> confirmMfaSetup(@Valid @RequestBody MfaSetupConfirmDTO dto) {
        return ResponseEntity.ok(authFactorService.confirmSetup(dto));
    }

    @PostMapping("/config")
    public ResponseEntity<Void> configureMfa(@Valid @RequestBody MfaConfigRequestDTO dto,
                                             Authentication authentication) {
        authFactorService.configureMfa((String) authentication.getPrincipal(), dto.enable2fa(), dto.enable3fa());
        return ResponseEntity.ok().build();
    }
}