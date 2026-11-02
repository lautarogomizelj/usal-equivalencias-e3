package com.usal.e3.security;

import com.usal.e3.service.SesionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

/** Autentica cada pedido con la cookie de sesión. Sin cookie válida el pedido sigue como anónimo. */
public class SesionFilter extends OncePerRequestFilter {

    private final SesionService sesiones;

    public SesionFilter(SesionService sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var cookie = WebUtils.getCookie(request, CookieSesion.NOMBRE);
        if (cookie != null) {
            sesiones.resolver(cookie.getValue()).ifPresent(actual -> SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(
                            actual, null, List.of(new SimpleGrantedAuthority("ROLE_" + actual.rol())))));
        }
        chain.doFilter(request, response);
    }
}
