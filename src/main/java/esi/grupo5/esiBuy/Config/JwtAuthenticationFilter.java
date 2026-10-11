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

import esi.grupo5.esiBuy.Repository.UsuarioRepository;
import esi.grupo5.esiBuy.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Autentica peticiones con cookies de acceso JWT y verifica que la cuenta siga disponible.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Servicio que valida y extrae los datos de los JWT. */
    private final JwtService jwtService;
    /** Persistencia consultada para revocar inmediatamente sesiones de cuentas no disponibles. */
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea el filtro con los servicios de tokens y consulta de cuentas.
     *
     * @param jwtService servicio de validación y lectura de JWT
     * @param usuarioRepository repositorio usado para comprobar el estado actual de la cuenta
     */
    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca la cookie {@code accessToken}, valida el JWT y establece la autenticación
     * únicamente si la cuenta continúa activa, desbloqueada y no eliminada. Así se revocan
     * inmediatamente los tokens de acceso tras la baja, el bloqueo o la desactivación.
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

        if (token != null && this.jwtService.isTokenValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String id = this.jwtService.extractId(token);
            if (usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalseAndEliminadoFalse(id)) {
                String rol = this.jwtService.extractRol(token);
                List<GrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        id,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}