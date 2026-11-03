package com.usal.e3.api.auth;

import com.usal.e3.security.CookieSesion;
import com.usal.e3.service.AuthService;
import com.usal.e3.service.SesionIniciada;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Reemplaza a Google mientras no haya credenciales (Sprint Planning, riesgos). No existe fuera de `dev`. */
@RestController
@Profile("dev")
public class DevAuthController {

    private final AuthService auth;
    private final CookieSesion cookie;

    public DevAuthController(AuthService auth, CookieSesion cookie) {
        this.auth = auth;
        this.cookie = cookie;
    }

    @PostMapping("/auth/dev/login")
    ResponseEntity<SesionResponse> login(@Valid @RequestBody EmailRequest body, HttpServletRequest request) {
        SesionIniciada sesion =
                auth.loginDesarrollo(Validaciones.normalizarEmail(body.email()), AuthController.cliente(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.crear(sesion.token(), sesion.duracion()))
                .body(SesionResponse.de(sesion.datos()));
    }
}
