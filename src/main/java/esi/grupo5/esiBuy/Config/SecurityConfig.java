package esi.grupo5.esiBuy.Config;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;

/** Configura la cadena de seguridad, el codificador y la autenticación JWT. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String ROL_VENDEDOR = "VENDEDOR";
    @Value("${app.security.csrf-enabled:false}")
    private boolean csrfEnabled;
    @Value("${app.cookie.secure:false}")
    private boolean secureCookies;
    @Value("${app.cookie.same-site:Strict}")
    private String cookieSameSite;
    @Value("${app.cookie.partitioned:false}")
    private boolean partitionedCookies;

    /** Filtro que establece la identidad y el rol a partir de la cookie JWT. */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Evita que Spring Boot registre el filtro también como filtro de servlet independiente.
     *
     * @param filter filtro JWT que se deshabilitará en el registro de servlet
     * @return registro del filtro deshabilitado
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    /**
     * Proporciona el codificador BCrypt para contraseñas.
     *
     * @return codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Crea la configuración con el filtro JWT de la aplicación.
     *
     * @param jwtAuthenticationFilter filtro de autenticación JWT
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Define la política HTTP, los endpoints públicos y los permisos por rol, e inserta
     * el filtro JWT antes de la autenticación estándar.
     *
     * @param http configuración HTTP de Spring Security
     * @return cadena de filtros configurada
     * @throws Exception si no se puede construir la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> {
                if (csrfEnabled) {
                    CsrfTokenRepository repository;
                    if (partitionedCookies) {
                        repository = new PartitionedCookieCsrfTokenRepository(secureCookies, cookieSameSite);
                    } else {
                        CookieCsrfTokenRepository standardRepository =
                                CookieCsrfTokenRepository.withHttpOnlyFalse();
                        standardRepository.setCookieCustomizer(cookie -> cookie
                                .secure(secureCookies)
                                .sameSite(cookieSameSite));
                        repository = standardRepository;
                    }
                    csrf.csrfTokenRepository(repository)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                            // Logout only expires this browser's auth cookies; a stale CSRF
                            // token must not prevent the user from ending their session.
                            .ignoringRequestMatchers("/users/logout");
                } else {
                    csrf.disable();
                }
            })

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ).exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) ->
                        this.writeAuthenticationRequired(response))
                .accessDeniedHandler(this::writeAccessDeniedResponse)
            )
            .authorizeHttpRequests(auth -> {

                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                auth.requestMatchers("/error").permitAll();
                auth.requestMatchers("/csrf").permitAll();
                // Allow clearing auth cookies even when the access token has expired.
                auth.requestMatchers(HttpMethod.POST, "/users/logout").permitAll();

                auth.requestMatchers(
                    "/users/login",
                    "/users/refresh",
                    "/users/register/**",
                    "/users/recover-password",
                    "/users/reset-password",
                    "/mfa/verify-mfa",
                    "/mfa/setup-init",
                    "/mfa/setup-confirm"
                ).permitAll();

                auth.requestMatchers(HttpMethod.GET, "/products/disponibles").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/products/*/info").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/products/*/valoraciones")
                    .hasRole(ROL_CLIENTE);
                auth.requestMatchers(HttpMethod.GET, "/categorias").permitAll();

                auth.requestMatchers(HttpMethod.POST, "/users/administradores")
                    .hasRole(ROL_ADMINISTRADOR);
                auth.requestMatchers(HttpMethod.PATCH, "/users/*")
                    .hasRole(ROL_ADMINISTRADOR);
                auth.requestMatchers("/api/admin/usuarios/**")
                    .hasRole(ROL_ADMINISTRADOR);
                auth.requestMatchers("/mfa/config").authenticated();

                auth.requestMatchers("/admin/**")
                    .hasRole(ROL_ADMINISTRADOR);

                auth.requestMatchers("/favoritos/**")
                    .hasRole(ROL_CLIENTE);
                auth.requestMatchers("/listas-deseos/**")
                    .hasRole(ROL_CLIENTE);
                auth.requestMatchers("/carrito", "/carrito/**")
                    .hasRole(ROL_CLIENTE);

                auth.requestMatchers("/pedidos/vendedor", "/pedidos/vendedor/**")
                    .hasRole(ROL_VENDEDOR);
                auth.requestMatchers(HttpMethod.PATCH, "/pedidos/*/enviar")
                    .hasRole(ROL_VENDEDOR);

                auth.requestMatchers("/pedidos/**")
                    .hasRole(ROL_CLIENTE);
                    
                auth.requestMatchers("/users/me/metodoPago/**")
                    .hasRole(ROL_CLIENTE);
                auth.requestMatchers("/users/me/direcciones/**")
                    .hasRole(ROL_CLIENTE);
                    
                auth.requestMatchers("/products/**")
                    .hasRole(ROL_VENDEDOR);

                auth.anyRequest().authenticated();
            });

        // Run JWT authentication after session management. Otherwise Spring can mistake
        // the per-request JWT authentication for a new login and clear the CSRF cookie.
        http.addFilterAfter(
            this.jwtAuthenticationFilter,
            SessionManagementFilter.class
        );

        return http.build();
    }

    private void writeAuthenticationRequired(HttpServletResponse response) throws IOException {
        String requestId = UUID.randomUUID().toString();
        Map<String, String> error = Map.of(
                "requestId", requestId,
                "error", "authentication_required",
                "message", "Inicia sesión para continuar.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Request-ID", requestId);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

    /** Writes a minimal access-denied response with a user-facing message. */
    private void writeAccessDeniedResponse(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        boolean csrfFailure = exception instanceof MissingCsrfTokenException
                || exception instanceof InvalidCsrfTokenException;
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String requestId = UUID.randomUUID().toString();
        String reason = exception instanceof MissingCsrfTokenException ? "csrf_cookie_missing"
                : exception instanceof InvalidCsrfTokenException ? "csrf_token_mismatch"
                : (authentication == null || !authentication.isAuthenticated()
                    ? "authentication_required" : "role_forbidden");
        String message = csrfFailure
                ? "No se pudo validar la solicitud. Recarga la página e inténtalo de nuevo."
                : "No tienes permisos para realizar esta acción.";
        Map<String, String> error = Map.of(
                "requestId", requestId,
                "error", "access_denied",
                "reason", reason,
                "message", message);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Request-ID", requestId);
        LOGGER.warn("Access denied requestId={} reason={} method={}",
                requestId, reason, request.getMethod());
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
