package esi.grupo5.esiBuy.Util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.HttpServletResponse;

/** Construye y añade cookies de tokens de autenticación a respuestas HTTP. */
@Component
public class CookieUtil {

    /** Servicio que proporciona las duraciones configuradas de los tokens. */
    private final JwtService jwtService;
    private final boolean secure;
    private final String sameSite;

    /**
     * Crea la utilidad de cookies.
     *
     * @param jwtService servicio de tokens JWT
     * @param secure indica si las cookies requieren HTTPS
     * @param sameSite política SameSite aplicada a las cookies
     */
    public CookieUtil(
            JwtService jwtService,
            @Value("${app.cookie.secure:false}") boolean secure,
            @Value("${app.cookie.same-site:Strict}") String sameSite) {
        this.jwtService = jwtService;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /**
     * Añade cookies HttpOnly para los tokens de acceso y refresco, aplicando los atributos
     * de seguridad configurados para el entorno.
     *
     * @param response respuesta en la que se añaden las cabeceras {@code Set-Cookie}
     * @param lr respuesta de autenticación que contiene ambos tokens
     */
    public void setTokenCookies(HttpServletResponse response, LoginResponseDTO lr) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", lr.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getAccessTokenExpirationSeconds())
                .sameSite(sameSite)
                .secure(secure)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", lr.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(jwtService.getRefreshTokenExpirationSeconds())
                .sameSite(sameSite)
                .secure(secure)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    /**
     * Añade cookies solo si la respuesta de autenticación contiene un token de acceso.
     *
     * @param response respuesta HTTP donde se pueden añadir cookies
     * @param loginResponse respuesta de autenticación que puede contener tokens
     */
    public void setTokenCookiesIfPresent(HttpServletResponse response, LoginResponseDTO loginResponse) {
        if (loginResponse.accessToken() != null) {
            setTokenCookies(response, loginResponse);
        }
    }

    /**
     * Expira las cookies de acceso y refresco; el atributo {@code Secure} coincide con
     * el utilizado al crearlas.
     *
     * @param response respuesta HTTP donde se sobrescriben las cookies
     */
    public void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .secure(secure)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .secure(secure)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}