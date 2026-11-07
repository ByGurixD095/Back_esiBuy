package esi.grupo5.esiBuy.Config;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** CSRF token repository that writes Partitioned as a raw Set-Cookie attribute. */
public class PartitionedCookieCsrfTokenRepository implements CsrfTokenRepository {

    private static final String COOKIE_NAME = "ESIBUY-XSRF-TOKEN";
    private static final String HEADER_NAME = "X-XSRF-TOKEN";
    private static final String PARAMETER_NAME = "_csrf";

    private final boolean secure;
    private final String sameSite;

    public PartitionedCookieCsrfTokenRepository(boolean secure, String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, UUID.randomUUID().toString());
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getContextPath().isEmpty() ? "/" : request.getContextPath();
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token == null ? "" : token.getToken())
                .path(path)
                .httpOnly(false)
                .secure(secure)
                .sameSite(sameSite)
                .partitioned(true)
                .maxAge(token == null ? 0 : -1)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
