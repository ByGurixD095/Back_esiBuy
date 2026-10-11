package esi.grupo5.esiBuy.Config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Autentica peticiones a partir de la cookie de acceso JWT cuando existe y es válida. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Servicio que valida y extrae los datos de los JWT. */
    private JwtService jwtService;

    /**
     * Crea el filtro con el servicio de gestión de tokens.
     *
     * @param jwtService servicio de validación y lectura de JWT
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Busca la cookie {@code accessToken}, establece la autenticación si el JWT es válido
     * y continúa la cadena de filtros.
     *
     * @param request petición HTTP que puede contener la cookie de acceso
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros que continuará procesando la petición
     * @throws ServletException si falla el procesamiento de la cadena de filtros
     * @throws IOException si ocurre un error de entrada/salida durante el filtrado
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && this.jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            String id = this.jwtService.extractId(token);
            String rol = this.jwtService.extractRol(token);

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    id, 
                    null,
                    authorities
            );
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}