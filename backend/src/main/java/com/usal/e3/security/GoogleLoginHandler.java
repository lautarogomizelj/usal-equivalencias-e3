package com.usal.e3.security;

import com.usal.e3.exception.ApiException;
import com.usal.e3.service.AuthService;
import com.usal.e3.service.ClienteHttp;
import com.usal.e3.service.SesionIniciada;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** Cierre del login con Google (contrato §4.1): crea la sesión propia y vuelve al frontend. */
@Component
public class GoogleLoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final AuthService auth;
    private final CookieSesion cookie;
    private final String frontendUrl;

    public GoogleLoginHandler(AuthService auth, CookieSesion cookie, @Value("${e3.frontend-url}") String frontendUrl) {
        this.auth = auth;
        this.cookie = cookie;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OidcUser google = (OidcUser) authentication.getPrincipal();
        // La sesión HTTP solo guardó el `state` de OAuth: de acá en más manda la cookie propia.
        HttpSession sesionHttp = request.getSession(false);
        if (sesionHttp != null) {
            sesionHttp.invalidate();
        }
        try {
            SesionIniciada sesion = auth.loginGoogle(
                    google.getEmail(),
                    google.getSubject(),
                    Boolean.TRUE.equals(google.getEmailVerified()),
                    new ClienteHttp(request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT)));
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.crear(sesion.token(), sesion.duracion()));
            response.sendRedirect(frontendUrl + "/dashboard");
        } catch (ApiException ex) {
            response.sendRedirect(frontendUrl + "/login?error=" + ex.code().name());
        }
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        response.sendRedirect(frontendUrl + "/login?error=GOOGLE_ERROR");
    }
}
