package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.SolicitudService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solicitudes y sus acciones (contrato §6 y §7). Qué rol puede cada ruta está en `SecurityConfig`. En el Sprint 3
 * el listado es solo el del aspirante: la bandeja del personal llega en el Sprint 4.
 */
@RestController
@RequestMapping("/solicitudes")
public class SolicitudesController {

    private static final int MAX_TAMANO_PAGINA = 100;

    private final SolicitudService solicitudes;

    public SolicitudesController(SolicitudService solicitudes) {
        this.solicitudes = solicitudes;
    }

    @GetMapping
    PaginaResponse<SolicitudResumenResponse> listar(
            @AuthenticationPrincipal SesionActual actual,
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) Boolean finalizadas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(
                solicitudes.delAspirante(actual, estado, finalizadas, pagina), SolicitudResumenResponse::de);
    }

    @PostMapping
    ResponseEntity<SolicitudDetalleResponse> crear(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody CrearSolicitudRequest body,
            HttpServletRequest request) {
        DatosOrigen origen = new DatosOrigen(
                body.universidadOrigen().strip(),
                body.facultadOrigen().strip(),
                body.carreraOrigen().strip(),
                body.condicionAcademica().strip(),
                body.observaciones() == null || body.observaciones().isBlank() ? null : body.observaciones().strip());
        SolicitudDetalleResponse creada = SolicitudDetalleResponse.de(
                solicitudes.crear(actual, body.carreraDestinoCodigo(), origen, request.getRemoteAddr()));
        return ResponseEntity.created(URI.create(request.getContextPath() + "/solicitudes/" + creada.id()))
                .body(creada);
    }

    @GetMapping("/{id}")
    SolicitudDetalleResponse detalle(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        return SolicitudDetalleResponse.de(solicitudes.detalle(actual, id));
    }

    @GetMapping("/{id}/historial")
    List<EventoHistorialResponse> historial(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        return EventoHistorialResponse.de(solicitudes.historial(actual, id));
    }

    @PostMapping("/{id}/documentacion/enviar")
    SolicitudDetalleResponse enviarDocumentacion(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody AccionRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                solicitudes.enviarDocumentacion(actual, id, body.estadoEsperado(), request.getRemoteAddr()));
    }

    @PostMapping("/{id}/cancelar")
    SolicitudDetalleResponse cancelar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody AccionRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                solicitudes.cancelar(actual, id, body.estadoEsperado(), request.getRemoteAddr()));
    }
}
