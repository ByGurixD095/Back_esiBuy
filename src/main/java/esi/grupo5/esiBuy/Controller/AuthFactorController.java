package esi.grupo5.esiBuy.Controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaBackupCodesDTO;
import esi.grupo5.esiBuy.Dto.MfaConfigRequestDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupConfirmDTO;
import esi.grupo5.esiBuy.Dto.MfaSetupResponseDTO;
import esi.grupo5.esiBuy.Dto.MfaVerifyRequestDTO;
import esi.grupo5.esiBuy.Service.AuthFactorService;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/mfa")
public class AuthFactorController {

    private static final String STRICT = "Strict";

    private final AuthFactorService authFactorService;
    private final JwtService jwtService;

    public AuthFactorController(AuthFactorService authFactorService, JwtService jwtService) {
        this.authFactorService = authFactorService;
        this.jwtService = jwtService;
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponseDTO> verifyMFA(
            @Valid @RequestBody MfaVerifyRequestDTO dto, HttpServletResponse response) {
        LoginResponseDTO loginResponse = authFactorService.verifyMFA(dto);
        setTokenCookies(response, loginResponse);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/setup-init")
    public ResponseEntity<MfaSetupResponseDTO> initMfaSetup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email es requerido");
        }
        return ResponseEntity.ok(authFactorService.initMfaSetup(email));
    }

    @PostMapping("/setup-confirm")
    public ResponseEntity<MfaBackupCodesDTO> confirmMfaSetup(
            @Valid @RequestBody MfaSetupConfirmDTO dto) {
        return ResponseEntity.ok(authFactorService.confirmMfaSetup(dto));
    }

    @PostMapping("/config")
    public ResponseEntity<Void> configureMfa(@Valid @RequestBody MfaConfigRequestDTO dto) {
        authFactorService.configureMfa(dto);
        return ResponseEntity.ok().build();
    }

    private void setTokenCookies(HttpServletResponse response, LoginResponseDTO loginResponse) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", loginResponse.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getAccessTokenExpirationSeconds())
                .sameSite(STRICT)
                .secure(false)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getRefreshTokenExpirationSeconds())
                .sameSite(STRICT)
                .secure(false)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
