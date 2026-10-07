package esi.grupo5.esiBuy.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Evita que Spring Boot registre el filtro JWT también como filtro de servlet
    // (solo debe ejecutarse dentro de la cadena de Spring Security)
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            // La API no guarda sesiones: cada peticion protegida debe llevar un JWT valido.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // Las peticiones OPTIONS se usan en la comprobacion previa de CORS.
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                auth.requestMatchers("/error").permitAll();

                // Solo las operaciones de autenticacion y registro son publicas.
                auth.requestMatchers(
                    "/users/login",
                    "/users/refresh",
                    "/users/register/**",
                    "/users/recover-password",
                    "/users/reset-password"
                ).permitAll();

                // Las operaciones de administracion requieren un JWT de administrador.
                auth.requestMatchers(HttpMethod.POST, "/users/administradores")
                    .hasRole("ADMINISTRADOR");
                auth.requestMatchers(HttpMethod.PATCH, "/users/*")
                    .hasRole("ADMINISTRADOR");
                auth.requestMatchers("/api/admin/usuarios/**")
                    .hasRole("ADMINISTRADOR");

                // Solo los vendedores autenticados pueden crear productos.
                auth.requestMatchers(HttpMethod.POST, "/products", "/products/createProduct")
                    .hasRole("VENDEDOR");

                // Cualquier otra ruta requiere que exista un usuario autenticado.
                auth.anyRequest().authenticated();
            });

        // Add JWT filter before the username/password auth filter
        http.addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}