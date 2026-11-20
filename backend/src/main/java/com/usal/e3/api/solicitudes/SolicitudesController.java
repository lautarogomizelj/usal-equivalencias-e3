package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.FiltroBandeja;
import com.usal.e3.service.ProcesoService;
import com.usal.e3.service.RevisionService;
import com.usal.e3.service.SolicitudService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solicitudes y sus acciones (contrato §6 y §7). Qué rol puede cada ruta está en `SecurityConfig`. */
@RestController
@RequestMapping("/solicitudes")
public class SolicitudesController {

    private static final int MAX_TAMANO_PAGINA = 100;

    private final SolicitudService solicitudes;
    private final ProcesoService proceso;
    private final RevisionService revision;

    public SolicitudesController(SolicitudService solicitudes, ProcesoService proceso, RevisionService revision) {
        this.solicitudes = solicitudes;
        this.proceso = proceso;
        this.revision = revision;
    }

    /** El aspirante ve las suyas, con `estado` y `finalizadas`; el personal, la bandeja con todos los filtros. */
    @GetMapping
    PaginaResponse<SolicitudResumenResponse> listar(
            @AuthenticationPrincipal SesionActual actual,
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) Integer carreraId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean asignadas,
            @RequestParam(defaultValue = "false") boolean sinResponsable,
            @RequestParam(required = false) Boolean finalizadas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(
                actual.tipo() == TitularTipo.ASPIRANTE
                        ? solicitudes.delAspirante(actual, estado, finalizadas, pagina)
                        : proceso.bandeja(
                                actual,
                                new FiltroBandeja(
                                        estado, carreraId, desde, hasta, q, asignadas, sinResponsable, finalizadas),
                                pagina),
                SolicitudResumenResponse::de);
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

    @PostMapping("/{id}/documentacion/aceptar")
    SolicitudDetalleResponse aceptarDocumentacion(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody MarcasRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                revision.aceptar(actual, id, body.estadoEsperado(), body.aMarcas(), request.getRemoteAddr()));
    }

    @PostMapping("/{id}/documentacion/rechazar")
    SolicitudDetalleResponse rechazarDocumentacion(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody MarcasRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(revision.rechazar(
                actual, id, body.estadoEsperado(), body.comentario(), body.aMarcas(), request.getRemoteAddr()));
    }

    @PostMapping("/{id}/responsables/tomar")
    SolicitudDetalleResponse tomarProceso(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, HttpServletRequest request) {
        return SolicitudDetalleResponse.de(proceso.tomar(actual, id, request.getRemoteAddr()));
    }

    @PostMapping("/{id}/responsables/baja")
    SolicitudDetalleResponse darseDeBaja(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, HttpServletRequest request) {
        return SolicitudDetalleResponse.de(proceso.darseDeBaja(actual, id, request.getRemoteAddr()));
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
