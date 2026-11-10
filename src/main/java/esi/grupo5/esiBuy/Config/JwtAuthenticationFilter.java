package esi.grupo5.esiBuy.Config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
 * Incluye una caché en memoria de corta duración para evitar sobrecargar MongoDB Atlas con
 * consultas redundantes en cada petición HTTP.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Servicio que valida y extrae los datos de los JWT. */
    private final JwtService jwtService;
    /** Persistencia consultada para revocar inmediatamente sesiones de cuentas no disponibles. */
    private final UsuarioRepository usuarioRepository;

    /** Caché en memoria para validaciones recientes de cuenta (id -> timestamp expiración en ms). */
    private final ConcurrentMap<String, Long> validUserCache = new ConcurrentHashMap<>();

    /** Tiempo de vida en segundos de la verificación de usuario en memoria (por defecto 20s). */
    @Value("${app.security.user-cache-seconds:20}")
    private long cacheDurationSeconds = 20;

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
     * únicamente si la cuenta continúa activa, desbloqueada y no eliminada.
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

        var currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        boolean noUserAuthentication = currentAuthentication == null
                || currentAuthentication instanceof AnonymousAuthenticationToken;

        if (token != null && this.jwtService.isTokenValid(token) && noUserAuthentication) {
            String id = this.jwtService.extractId(token);
            if (isUserActiveAndNotBlocked(id)) {
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

    /**
     * Comprueba si el usuario está activo y no bloqueado, consultando la caché local
     * de 20 segundos antes de acceder a la base de datos MongoDB.
     *
     * @param id identificador del usuario
     * @return true si el usuario existe, está activo y no bloqueado
     */
    private boolean isUserActiveAndNotBlocked(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        if (cacheDurationSeconds <= 0) {
            return usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse(id);
        }

        long now = System.currentTimeMillis();
        Long cachedUntil = validUserCache.get(id);
        if (cachedUntil != null && cachedUntil > now) {
            return true;
        }

        boolean activeAndNotBlocked = usuarioRepository.existsByIdAndActivoTrueAndBloqueadoFalse(id);
        if (activeAndNotBlocked) {
            validUserCache.put(id, now + (cacheDurationSeconds * 1000L));
        } else {
            validUserCache.remove(id);
        }

        return activeAndNotBlocked;
    }

    /** Invalida la caché de un usuario concreto (p. ej. tras bloqueo o eliminación). */
    public void invalidateUserCache(String id) {
        if (id != null) {
            validUserCache.remove(id);
        }
    }

    /** Limpia toda la caché de verificación de usuarios. */
    public void clearCache() {
        validUserCache.clear();
    }
}
