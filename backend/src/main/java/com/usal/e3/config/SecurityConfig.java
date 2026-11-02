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

/**
 * RBAC por rol. CSRF queda apagado hasta que `client.ts` mande el header del contrato (§1): mientras
 * tanto protegen SameSite=Lax, el CORS con origen fijo y que la API solo acepte JSON.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain seguridad(
            HttpSecurity http, SesionService sesiones, GoogleLoginHandler google, ErroresSeguridad errores)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
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
                                "/auth/google",
                                "/auth/google/callback",
                                "/auth/password/**",
                                "/auth/dev/**")
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
                        .anyRequest()
                        .authenticated());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
