package com.usal.e3.config;

import com.usal.e3.domain.Rol;
import com.usal.e3.security.ErroresSeguridad;
import com.usal.e3.security.GoogleLoginHandler;
import com.usal.e3.security.SesionFilter;
import com.usal.e3.service.SesionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * RBAC por rol y CSRF del contrato (§1): cookie `XSRF-TOKEN` que el frontend devuelve en el header
 * `X-XSRF-TOKEN` de cada POST, PUT, PATCH y DELETE.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain seguridad(
            HttpSecurity http, SesionService sesiones, GoogleLoginHandler google, ErroresSeguridad errores)
            throws Exception {
        // La cookie se lee desde el frontend, que corre en otra ruta que `/api`. Sin atributo de pedido el
        // token se carga en cada pedido, así que la cookie ya está antes del primer POST.
        CookieCsrfTokenRepository tokens = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokens.setCookiePath("/");
        CsrfTokenRequestAttributeHandler lector = new CsrfTokenRequestAttributeHandler();
        lector.setCsrfRequestAttributeName(null);
        http.csrf(csrf -> csrf.csrfTokenRepository(tokens).csrfTokenRequestHandler(lector))
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/google")
                        .redirectionEndpoint(r -> r.baseUri("/auth/google/callback"))
                        .successHandler(google)
                        .failureHandler(google))
                .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores))
                .addFilterBefore(new SesionFilter(sesiones), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(
                                "/auth/registro/**",
                                "/auth/login",
                                "/auth/admin/login",
                                "/auth/personal/login",
                                "/auth/google",
                                "/auth/google/callback",
                                "/auth/password/**")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole(Rol.ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/perfil")
                        .hasAnyRole(Rol.ASPIRANTE, Rol.ACADEMICO, Rol.SECRETARIA, Rol.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/perfil/password")
                        .hasAnyRole(Rol.ASPIRANTE, Rol.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/perfil/email")
                        .hasRole(Rol.ADMIN)
                        // En el Sprint 3 el listado es solo el del aspirante; la bandeja del personal llega en el 4.
                        .requestMatchers(HttpMethod.GET, "/solicitudes")
                        .hasRole(Rol.ASPIRANTE)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/solicitudes",
                                "/solicitudes/*/documentos",
                                "/solicitudes/*/documentacion/enviar",
                                "/solicitudes/*/cancelar")
                        .hasRole(Rol.ASPIRANTE)
                        .requestMatchers(HttpMethod.DELETE, "/solicitudes/*/documentos/*")
                        .hasRole(Rol.ASPIRANTE)
                        .anyRequest()
                        .authenticated());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
