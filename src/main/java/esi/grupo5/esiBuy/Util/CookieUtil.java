package esi.grupo5.esiBuy.Util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import esi.grupo5.esiBuy.Dto.LoginResponseDTO;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private static final String STRICT = "Strict";
    private final JwtService jwtService;

    public CookieUtil(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void setTokenCookies(HttpServletResponse response, LoginResponseDTO lr) {
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
                .secure(false) // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(STRICT)
                .secure(false) // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(STRICT)
                .secure(false) // TODO: PONER A TRUE EN PRODUCCIÓN (HTTPS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}