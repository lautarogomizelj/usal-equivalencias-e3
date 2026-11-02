package com.usal.e3.api.auth;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.security.CookieSesion;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AuthService;
import com.usal.e3.service.ClienteHttp;
import com.usal.e3.service.SesionIniciada;
import com.usal.e3.service.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación del contrato de la API (§4.1). El callback de Google lo atiende Spring Security. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;
    private final SesionService sesiones;
    private final CookieSesion cookie;

    public AuthController(AuthService auth, SesionService sesiones, CookieSesion cookie) {
        this.auth = auth;
        this.sesiones = sesiones;
        this.cookie = cookie;
    }

    static ClienteHttp cliente(HttpServletRequest request) {
        return new ClienteHttp(request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    RegistroResponse registrar(@Valid @RequestBody RegistroRequest body, HttpServletRequest request) {
        Aspirante aspirante = auth.registrar(
                Validaciones.normalizarEmail(body.email()),
                body.password(),
                body.nombre().trim(),
                body.telefono().trim(),
                body.dni().trim(),
                cliente(request));
        return new RegistroResponse(aspirante.getId(), aspirante.getEmail());
    }

    @PostMapping("/registro/validar")
    ResponseEntity<SesionResponse> validar(@Valid @RequestBody ValidarCodigoRequest body, HttpServletRequest request) {
        return conCookie(auth.validarRegistro(
                Validaciones.normalizarEmail(body.email()), body.codigo().trim(), cliente(request)));
    }

    @PostMapping("/registro/reenviar-codigo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reenviarCodigo(@Valid @RequestBody EmailRequest body) {
        auth.reenviarCodigo(Validaciones.normalizarEmail(body.email()));
    }

    @PostMapping("/login")
    ResponseEntity<SesionResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        return conCookie(auth.loginAspirante(
                Validaciones.normalizarEmail(body.email()), body.password(), body.recordarme(), cliente(request)));
    }

    @PostMapping("/admin/login")
    ResponseEntity<SesionResponse> loginAdmin(@Valid @RequestBody AdminLoginRequest body, HttpServletRequest request) {
        return conCookie(
                auth.loginAdmin(Validaciones.normalizarEmail(body.usuario()), body.password(), cliente(request)));
    }

    @GetMapping("/google")
    void google(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/google");
    }

    @PostMapping("/password/recuperar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void recuperar(@Valid @RequestBody EmailRequest body) {
        auth.recuperarPassword(Validaciones.normalizarEmail(body.email()));
    }

    @PostMapping("/password/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void restablecer(@Valid @RequestBody RestablecerRequest body, HttpServletRequest request) {
        auth.restablecerPassword(
                Validaciones.normalizarEmail(body.email()), body.codigo().trim(), body.passwordNueva(), cliente(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal SesionActual actual) {
        sesiones.cerrar(actual, actual.sesionId());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.borrar()).build();
    }

    @GetMapping("/me")
    SesionResponse me(@AuthenticationPrincipal SesionActual actual) {
        return SesionResponse.de(sesiones.datos(actual));
    }

    private ResponseEntity<SesionResponse> conCookie(SesionIniciada sesion) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.crear(sesion.token(), sesion.duracion()))
                .body(SesionResponse.de(sesion.datos()));
    }
}
