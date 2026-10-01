package esi.grupo5.esiBuy.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import esi.grupo5.esiBuy.Service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired 
    private UserService userService;

    @Autowired 
    private JwtService jwtService;
    
    // GET 
    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        //TODO: Implementar
        return ResponseEntity.ok("In progress");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUserById(@PathVariable int id) {
        //TODO: Implementar
        return ResponseEntity.ok("In progress");
    }

    // POST
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody String name) {
        //TODO: Implemetnar
        return ResponseEntity.ok("Usuario creado: " + name);
    }

    @PostMapping("/clientes")
    public ResponseEntity<AuthResponseDTO> registrarCliente(@Valid @RequestBody ClienteRegistroDTO dto) {
        AuthResponseDTO authResponse = usuarioService.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/register/vendedor")
    public ResponseEntity<AuthResponseDTO> registerVendedor(
            @Valid @RequestBody VendedorRegisterRequest request) {

        AuthResponseDTO response = usuarioService.registerVendedor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // DELETE 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        //TODO: Implementar
        return ResponseEntity.notFound().build();
    }

    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto, HttpServletResponse response) {

        LoginResponseDTO loginResponse = this.userService.login(dto);
        setTokenCookies(response, loginResponse);

        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        
        if (refreshToken == null) {
            throw new RuntimeException("No refresh token provided");
        }

        LoginResponseDTO lr = this.userService.refreshToken(refreshToken);

        setTokenCookies(response, lr);
        return ResponseEntity.ok(lr);
    }

    private void setTokenCookies(HttpServletResponse response, LoginResponseDTO lr) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", lr.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getAccessTokenExpirationSeconds())
                .sameSite("Strict")
                .secure(false) // PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", lr.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getRefreshTokenExpirationSeconds())
                .sameSite("Strict")
                .secure(false) // PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    //ESTO LUEGO LO USAMOS PARA EL LOGOUT/CERRAR SESIÓN
    private void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)          // Fuerza el borrado inmediato
                .sameSite("Strict")
                .secure(false)       // PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)          // Fuerza el borrado inmediato
                .sameSite("Strict")
                .secure(false)       // PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
    
}    