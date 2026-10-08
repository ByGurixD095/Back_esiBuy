package esi.grupo5.esiBuy.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Evita que Spring Boot registre el filtro JWT también como filtro de servlet
    // (solo debe ejecutarse dentro de la cadena de Spring Security)
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            // La API no guarda sesiones.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> {

                // CORS
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                // Errores
                auth.requestMatchers("/error").permitAll();

                // -------------------------------------------------
                // ENDPOINTS PÚBLICOS
                // -------------------------------------------------
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

                auth.requestMatchers("/mfa/config").authenticated();

                // -------------------------------------------------
                // ADMINISTRADOR
                // -------------------------------------------------
                auth.requestMatchers("/admin/**")
                    .hasRole("ADMINISTRADOR");

                // -------------------------------------------------
                // VENDEDOR
                // -------------------------------------------------

                auth.requestMatchers("/products/**")
                    .hasRole("VENDEDOR");

                // -------------------------------------------------
                // RESTO DE ENDPOINTS
                // -------------------------------------------------
                auth.anyRequest().authenticated();
            });

        // Filtro JWT antes del filtro de autenticación estándar
        http.addFilterBefore(
            this.jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}