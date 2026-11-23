package com.usal.e3.api.notificaciones;

import com.usal.e3.api.solicitudes.PaginaResponse;
import com.usal.e3.domain.Notificacion;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.CampanaService;
import java.time.OffsetDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Campana de notificaciones (contrato §8.2). Para todos los roles. */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionesController {

    private static final int MAX_TAMANO_PAGINA = 50;

    record NotificacionResponse(
            Long id, String titulo, String mensaje, Integer solicitudId, boolean leida, OffsetDateTime fechaCreacion) {

        static NotificacionResponse de(Notificacion n) {
            return new NotificacionResponse(
                    n.getId(), n.getTitulo(), n.getMensaje(), n.getSolicitudId(), n.isLeida(), n.getFechaCreacion());
        }
    }

    record CantidadResponse(long cantidad) {}

    private final CampanaService campana;

    public NotificacionesController(CampanaService campana) {
        this.campana = campana;
    }

    @GetMapping
    PaginaResponse<NotificacionResponse> listar(
            @AuthenticationPrincipal SesionActual actual,
            @RequestParam(required = false) Boolean leida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(campana.listar(actual, leida, pagina), NotificacionResponse::de);
    }

    @GetMapping("/no-leidas/cantidad")
    CantidadResponse noLeidas(@AuthenticationPrincipal SesionActual actual) {
        return new CantidadResponse(campana.noLeidas(actual));
    }

    /** El contrato manda `{ "leida": true }`: no hay otra cosa que cambiar, así que el cuerpo no se lee. */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarLeida(@AuthenticationPrincipal SesionActual actual, @PathVariable Long id) {
        campana.marcarLeida(actual, id);
    }

    @PostMapping("/marcar-todas-leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarTodasLeidas(@AuthenticationPrincipal SesionActual actual) {
        campana.marcarTodasLeidas(actual);
    }
}
