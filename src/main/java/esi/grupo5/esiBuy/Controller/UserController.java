package esi.grupo5.esiBuy.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final String STRICT = "Strict";

    private UserService userService;

    private JwtService jwtService;
    
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // --------- GET ------------ 

    //Se eliminan todos los métodos que aún no se implementar para evitar confusiones

    // --------- POST ------------ 
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto, HttpServletResponse response) {

        LoginResponseDTO loginResponse = this.userService.login(dto);
        setTokenCookies(response, loginResponse);

        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No refresh token provided");
        }

        LoginResponseDTO lr = this.userService.refreshToken(refreshToken);

        setTokenCookies(response, lr);
        return ResponseEntity.ok(lr);
    }

    @PostMapping("/register/clientes")
    public ResponseEntity<LoginResponseDTO> registrarCliente(
            @Valid @RequestBody ClienteRegistroDTO dto, 
            HttpServletResponse response) {
        
        LoginResponseDTO loginResponse = userService.registrarCliente(dto);
        setTokenCookies(response, loginResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @PostMapping("/register/vendedor")
    public ResponseEntity<LoginResponseDTO> registerVendedor(
            @Valid @RequestBody VendedorRegisterRequest request, 
            HttpServletResponse response) {

        LoginResponseDTO loginResponse = userService.registrarVendedor(request);
        setTokenCookies(response, loginResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    // --------- DELETE ------------ 


    // --------- PRIVATE METHODS ------------ 
    private void setTokenCookies(HttpServletResponse response, LoginResponseDTO lr) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", lr.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getAccessTokenExpirationSeconds())
                .sameSite(STRICT)
                .secure(false) // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", lr.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getRefreshTokenExpirationSeconds())
                .sameSite(STRICT)
                .secure(false) // PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    //TODO:  ESTO LUEGO LO USAMOS PARA EL LOGOUT/CERRAR SESIÓN
    private void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)          // Fuerza el borrado inmediato
                .sameSite(STRICT)
                .secure(false)       // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)          // Fuerza el borrado inmediato
                .sameSite(STRICT)
                .secure(false)       // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
    
}    