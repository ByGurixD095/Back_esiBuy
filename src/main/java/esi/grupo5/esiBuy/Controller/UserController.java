package esi.grupo5.esiBuy.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esi.grupo5.esiBuy.Dto.ClienteRegistroDTO;
import esi.grupo5.esiBuy.Dto.LoginRequestDTO;
import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetConfirmDTO;
import esi.grupo5.esiBuy.Dto.PasswordResetRequestDTO;
import esi.grupo5.esiBuy.Dto.UsuarioResponseDTO;
import esi.grupo5.esiBuy.Dto.UserSelfUpdateDTO;
import esi.grupo5.esiBuy.Dto.VendedorRegisterRequest;
import esi.grupo5.esiBuy.Exception.ValidationException;
import esi.grupo5.esiBuy.Service.AuthService;
import esi.grupo5.esiBuy.Service.UserService;
import esi.grupo5.esiBuy.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/** Expone operaciones de cuenta, autenticación, registro y perfil de usuario. */
@RestController
@RequestMapping("/users")
public class UserController {

    /** Servicio de registro, consulta y actualización de perfiles. */
    private final UserService userService;
    /** Servicio de autenticación, tokens y recuperación de contraseña. */
    private final AuthService authService;
    /** Gestión de cookies de acceso y refresco. */
    private final CookieUtil cookieUtil;
    
    /**
     * Crea el controlador de usuarios.
     *
     * @param userService servicio de usuarios
     * @param authService servicio de autenticación
     * @param cookieUtil utilidad de cookies
     */
    public UserController(UserService userService, AuthService authService, CookieUtil cookieUtil) {
        this.userService = userService;
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    /**
     * Devuelve el perfil del usuario autenticado ({@code GET /users/me}).
     *
     * @param authentication identidad de la petición
     * @return perfil con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si la cuenta no existe o está eliminada
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getUserById(Authentication authentication) {
        String idAutenticado = (String) authentication.getPrincipal();
        UsuarioResponseDTO userDto = userService.getUserById(idAutenticado);
        return ResponseEntity.ok(userDto);
    }


    /**
     * Inicia sesión con las credenciales del cuerpo y añade las cookies de token si se emiten.
     *
     * @param dto credenciales de acceso
     * @param response respuesta donde pueden establecerse cookies
     * @param request petición de origen, usada para obtener la dirección IP
     * @return resultado del inicio de sesión con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.AuthException si las credenciales no son válidas
     * @throws esi.grupo5.esiBuy.Exception.ForbiddenException si la cuenta no está disponible
     * @throws esi.grupo5.esiBuy.Exception.PasswordExpiredException si la contraseña ha caducado
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto, HttpServletResponse response, HttpServletRequest request) {
        LoginResponseDTO loginResponse = authService.login(dto, request.getRemoteAddr());
        cookieUtil.setTokenCookiesIfPresent(response, loginResponse);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Renueva el token de acceso a partir de la cookie opcional {@code refreshToken}.
     *
     * @param refreshToken cookie de refresco, que debe estar presente y no vacía
     * @param response respuesta donde se establecen las cookies renovadas
     * @return resultado de renovación con estado HTTP 200
     * @throws ValidationException si falta la cookie de refresco
     * @throws esi.grupo5.esiBuy.Exception.AuthException si el token no es válido
     * @throws esi.grupo5.esiBuy.Exception.ExpiredTokenException si el token ha caducado
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("No refresh token provided");
        }

        LoginResponseDTO loginResponse = authService.refreshToken(refreshToken);
        cookieUtil.setTokenCookies(response, loginResponse);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Registra un cliente y establece cookies si se completa la autenticación.
     *
     * @param dto datos validados del cliente
     * @param response respuesta donde pueden establecerse cookies
     * @return resultado del registro con estado HTTP 201
     * @throws esi.grupo5.esiBuy.Exception.ConflictException si el correo ya está registrado
     */
    @PostMapping("/register/clientes")
    public ResponseEntity<LoginResponseDTO> registrarCliente(
            @Valid @RequestBody ClienteRegistroDTO dto, 
            HttpServletResponse response) {
        
        LoginResponseDTO loginResponse = userService.registrarCliente(dto);
        cookieUtil.setTokenCookiesIfPresent(response, loginResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    /**
     * Registra un vendedor y establece cookies si se completa la autenticación.
     *
     * @param request datos validados del vendedor
     * @param response respuesta donde pueden establecerse cookies
     * @return resultado del registro con estado HTTP 201
     * @throws esi.grupo5.esiBuy.Exception.ConflictException si el correo ya está registrado
     */
    @PostMapping("/register/vendedor")
    public ResponseEntity<LoginResponseDTO> registerVendedor(
            @Valid @RequestBody VendedorRegisterRequest request, 
            HttpServletResponse response) {

        LoginResponseDTO loginResponse = userService.registrarVendedor(request);
        cookieUtil.setTokenCookiesIfPresent(response, loginResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    /**
     * Solicita por correo un enlace para recuperar la contraseña.
     *
     * @param request correo electrónico de la cuenta
     * @return respuesta vacía con estado HTTP 200
     */
    @PostMapping("/recover-password")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Confirma el cambio de contraseña con el token de recuperación.
     *
     * @param request token y nueva contraseña
     * @return respuesta vacía con estado HTTP 200
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si el token no corresponde a una cuenta
     * @throws ValidationException si las contraseñas no coinciden o el token ha caducado
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Expira las cookies de acceso y refresco del navegador.
     *
     * @param response respuesta HTTP donde se eliminan las cookies
     * @return respuesta vacía con estado HTTP 200
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.clearTokenCookies(response);
        return ResponseEntity.ok().build();
    }

    /**
     * Actualiza los campos permitidos del perfil autenticado.
     *
     * @param dto campos opcionales que se modificarán
     * @param authentication identidad de la petición autenticada
     * @return respuesta sin cuerpo con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si la cuenta no existe o está eliminada
     */
    @PatchMapping("/me/update")
    public ResponseEntity<Void> modificarMiPerfil(
            @Valid @RequestBody UserSelfUpdateDTO dto,
            Authentication authentication) {
        
        String idAutenticado = (String) authentication.getPrincipal(); 
        userService.modificarMiPerfil(idAutenticado, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina lógicamente la cuenta autenticada, revoca sus tokens de refresco y expira
     * las cookies de sesión.
     *
     * @param authentication identidad de la petición autenticada
     * @param response respuesta HTTP en la que se expiran las cookies
     * @return respuesta vacía con estado HTTP 204
     * @throws esi.grupo5.esiBuy.Exception.NotFoundException si la cuenta no existe o ya está eliminada
     * @throws esi.grupo5.esiBuy.Exception.ForbiddenException si la cuenta es el último administrador disponible
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarMiCuenta(Authentication authentication, HttpServletResponse response) {
        String idAutenticado = (String) authentication.getPrincipal();
        userService.eliminarCuenta(idAutenticado);
        cookieUtil.clearTokenCookies(response);
        return ResponseEntity.noContent().build();
    }
}
