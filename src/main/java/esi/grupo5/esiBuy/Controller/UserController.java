package esi.grupo5.esiBuy.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    
    public UserController(UserService userService, AuthService authService, CookieUtil cookieUtil) {
        this.userService = userService;
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }


    // --------- POST ------------ 
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto, HttpServletResponse response, HttpServletRequest request) {
        LoginResponseDTO loginResponse = authService.login(dto, request.getRemoteAddr());
        
        if (loginResponse.accessToken() != null) {
            cookieUtil.setTokenCookies(response, loginResponse);
        }
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No refresh token provided");
        }

        LoginResponseDTO lr = authService.refreshToken(refreshToken);
        cookieUtil.setTokenCookies(response, lr);
        return ResponseEntity.ok(lr);
    }

    @PostMapping("/register/clientes")
    public ResponseEntity<LoginResponseDTO> registrarCliente(
            @Valid @RequestBody ClienteRegistroDTO dto, 
            HttpServletResponse response) {
        
        LoginResponseDTO loginResponse = userService.registrarCliente(dto);
        if (loginResponse.accessToken() != null) {
            cookieUtil.setTokenCookies(response, loginResponse);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @PostMapping("/register/vendedor")
    public ResponseEntity<LoginResponseDTO> registerVendedor(
            @Valid @RequestBody VendedorRegisterRequest request, 
            HttpServletResponse response) {

        LoginResponseDTO loginResponse = userService.registrarVendedor(request);
        if (loginResponse.accessToken() != null) {
            cookieUtil.setTokenCookies(response, loginResponse);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @PostMapping("/recover-password")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.clearTokenCookies(response);
        return ResponseEntity.ok().build();
    }

    // --------- PATCH ------------ 
    @PatchMapping("/me/update")
    public ResponseEntity<Void> modificarMiPerfil(
            @Valid @RequestBody UserSelfUpdateDTO dto,
            Authentication authentication) {
        
        String idAutenticado = (String) authentication.getPrincipal(); 
        
        userService.modificarMiPerfil(idAutenticado, dto);
        return ResponseEntity.noContent().build();
    }

}