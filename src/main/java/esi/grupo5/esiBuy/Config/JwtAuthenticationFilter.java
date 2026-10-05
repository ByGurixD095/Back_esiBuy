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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = null;

        // 1. Extraer las cookies de la petición
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                // Buscamos específicamente el accessToken
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Validar que el token existe, no ha caducado y la firma es correcta
        if (token != null && this.jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 3. Extraer los datos del token usando tu JwtService
            String id = this.jwtService.extractId(token);
            String rol = this.jwtService.extractRol(token);

            // 4. Crear la lista de autoridades (roles). Spring Security espera el prefijo "ROLE_" por defecto.
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));

            // 5. Crear el objeto de autenticación
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    id, 
                    null, // No pasamos la contraseña porque ya confiamos en la firma del token
                    authorities
            );
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 6. Registrar al usuario en el contexto de seguridad de Spring
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 7. Pasar la petición al siguiente filtro o controlador
        filterChain.doFilter(request, response);
    }
}