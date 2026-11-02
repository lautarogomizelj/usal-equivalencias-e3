package com.usal.e3.api.usuarios;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.PerfilService;
import com.usal.e3.service.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del contrato de la API (§4.2). Qué rol puede cada ruta está en `SecurityConfig`. */
@RestController
@RequestMapping("/perfil")
public class PerfilController {

    private final PerfilService perfil;
    private final SesionService sesiones;

    public PerfilController(PerfilService perfil, SesionService sesiones) {
        this.perfil = perfil;
        this.sesiones = sesiones;
    }

    @GetMapping
    PerfilResponse obtener(@AuthenticationPrincipal SesionActual actual) {
        return PerfilResponse.de(perfil.obtener(actual));
    }

    @PatchMapping
    PerfilResponse editar(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody EditarPerfilRequest body,
            HttpServletRequest request) {
        return PerfilResponse.de(
                perfil.editar(actual, body.nombre(), body.telefono(), body.avatar(), request.getRemoteAddr()));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiarPassword(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody CambiarPasswordRequest body,
            HttpServletRequest request) {
        perfil.cambiarPassword(actual, body.passwordActual(), body.passwordNueva(), request.getRemoteAddr());
    }

    @PutMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiarEmail(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody CambiarEmailRequest body,
            HttpServletRequest request) {
        perfil.cambiarEmail(
                actual,
                Validaciones.normalizarEmail(body.emailNuevo()),
                body.passwordActual(),
                request.getRemoteAddr());
    }

    @GetMapping("/accesos")
    List<AccesoResponse> accesos(@AuthenticationPrincipal SesionActual actual) {
        return sesiones.ultimosAccesos(actual).stream().map(AccesoResponse::de).toList();
    }

    @GetMapping("/sesiones")
    List<SesionActivaResponse> activas(@AuthenticationPrincipal SesionActual actual) {
        return sesiones.activas(actual).stream()
                .map(sesion -> SesionActivaResponse.de(sesion, actual.sesionId()))
                .toList();
    }

    @DeleteMapping("/sesiones/{sesionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cerrarSesion(@AuthenticationPrincipal SesionActual actual, @PathVariable UUID sesionId) {
        sesiones.cerrar(actual, sesionId);
    }
}
