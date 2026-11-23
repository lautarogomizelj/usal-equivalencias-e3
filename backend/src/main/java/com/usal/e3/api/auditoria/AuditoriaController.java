package com.usal.e3.api.auditoria;

import com.usal.e3.api.solicitudes.PaginaResponse;
import com.usal.e3.domain.EventoAuditoria;
import com.usal.e3.service.AuditoriaService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Auditoría general y de una solicitud (contrato §8.3). Solo Admin y Revisor: lo exige `SecurityConfig`. */
@RestController
public class AuditoriaController {

    private static final int MAX_TAMANO_PAGINA = 100;

    public record UsuarioRef(Integer id, String email) {}

    /** Los estados van siempre por código (RF-ESTADO-004). */
    public record EventoResponse(
            Long id,
            OffsetDateTime timestamp,
            UsuarioRef usuario,
            String accion,
            String entidad,
            Integer entidadId,
            String estadoCodigoAnterior,
            String estadoCodigoNuevo,
            Map<String, Object> detalles,
            String comentarios,
            String ipOrigen) {

        static EventoResponse de(EventoAuditoria e) {
            return new EventoResponse(
                    e.getId(),
                    e.getFecha(),
                    new UsuarioRef(e.getUsuarioId(), e.getUsuarioEmail()),
                    e.getAccion(),
                    e.getEntidad(),
                    e.getEntidadId(),
                    e.getEstadoCodigoAnterior(),
                    e.getEstadoCodigoNuevo(),
                    e.getDetalles(),
                    e.getComentarios(),
                    e.getIpOrigen());
        }
    }

    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping("/auditoria")
    PaginaResponse<EventoResponse> listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) Integer entidadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return pagina(new AuditoriaService.Filtro(usuario, accion, entidad, entidadId, desde, hasta), page, size);
    }

    /** Las acciones y entidades que ya tienen eventos, para los filtros de la pantalla. */
    @GetMapping("/auditoria/opciones")
    AuditoriaService.Opciones opciones() {
        return auditoria.opciones();
    }

    @GetMapping("/auditoria/{id}")
    EventoResponse detalle(@PathVariable Long id) {
        return EventoResponse.de(auditoria.evento(id));
    }

    /** Pestaña "Auditoría" del detalle de la solicitud. */
    @GetMapping("/solicitudes/{id}/auditoria")
    PaginaResponse<EventoResponse> deSolicitud(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return pagina(new AuditoriaService.Filtro(null, null, "solicitud", id, null, null), page, size);
    }

    private PaginaResponse<EventoResponse> pagina(AuditoriaService.Filtro filtro, int page, int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(auditoria.buscar(filtro, pagina), EventoResponse::de);
    }
}
