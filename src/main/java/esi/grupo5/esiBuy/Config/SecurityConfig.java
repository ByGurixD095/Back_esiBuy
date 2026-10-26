package esi.grupo5.esiBuy.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/** Configura la cadena de seguridad, el codificador y la autenticación JWT. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ROL_CLIENTE = "CLIENTE";

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
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ).exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado o no proporcionado")
                )
            )
            .authorizeHttpRequests(auth -> {

                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                auth.requestMatchers("/error").permitAll();

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
                auth.requestMatchers("/carrito/**")
                    .hasRole(ROL_CLIENTE);
                auth.requestMatchers("/users/me/metodoPago/**")
                    .hasRole(ROL_CLIENTE);
                    
                auth.requestMatchers("/products/**")
                    .hasRole("VENDEDOR");

                auth.anyRequest().authenticated();
            });

        http.addFilterBefore(
            this.jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}