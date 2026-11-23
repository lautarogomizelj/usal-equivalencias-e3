package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.EventoAuditoria;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Rol;
import com.usal.e3.service.HistorialSolicitud;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** `EventoHistorial` del contrato (§6.4): cambios de estado, cambios de responsable y documentos cargados. */
public record EventoHistorialResponse(
        OffsetDateTime fecha, String tipo, String estadoAnterior, String estadoNuevo, String usuario, String comentario) {

    /** Un único timeline, el más reciente primero (RF-AUDIT-002). */
    static List<EventoHistorialResponse> de(HistorialSolicitud historial) {
        String aspirante = historial.solicitud().getAspirante().nombreCompleto();
        Stream<EventoHistorialResponse> cambios = historial.cambios().stream().map(c -> cambio(c, aspirante));
        Stream<EventoHistorialResponse> responsables =
                historial.cambiosDeResponsable().stream().map(EventoHistorialResponse::responsable);
        Stream<EventoHistorialResponse> documentos =
                historial.documentosCargados().stream().map(d -> documento(d, aspirante));
        return Stream.of(cambios, responsables, documentos)
                .flatMap(eventos -> eventos)
                .sorted(Comparator.comparing(EventoHistorialResponse::fecha).reversed())
                .toList();
    }

    private static EventoHistorialResponse cambio(HistorialEstado cambio, String aspirante) {
        String usuario = cambio.getUsuarioUltimoCambio() == null
                ? aspirante
                : cambio.getUsuarioUltimoCambio().nombreCompleto();
        return new EventoHistorialResponse(
                cambio.getFechaAsignacion(),
                "CAMBIO_ESTADO",
                cambio.getEstadoAnteriorCodigo(),
                cambio.getEstadoCodigo(),
                usuario,
                cambio.getComentarios());
    }

    /**
     * Sale de la auditoría de "tomar proceso" y "darse de baja", que guarda el nombre y el rol, y de las dos del
     * Admin (RF-ADMIN-004 y 005), que además guardan a quién asignó o quitó.
     */
    private static EventoHistorialResponse responsable(EventoAuditoria evento) {
        String papel = Rol.ACADEMICO.equals(evento.getDetalles().get("rol"))
                ? "responsable académico"
                : "responsable administrativo";
        Object otro = evento.getDetalles().get("responsable");
        String comentario = switch (evento.getAccion()) {
            case "tomar_proceso" -> "Tomó el proceso como " + papel + ".";
            case "asignar_responsable" -> "Asignó a " + otro + " como " + papel + ".";
            case "quitar_responsable" -> "Revocó el permiso de " + otro + ", que dejó de ser " + papel + ".";
            default -> "Se dio de baja como " + papel + ".";
        };
        return new EventoHistorialResponse(
                evento.getFecha(), "CAMBIO_RESPONSABLE", null, null, (String) evento.getDetalles().get("usuario"), comentario);
    }

    private static EventoHistorialResponse documento(DocumentoSolicitud documento, String aspirante) {
        return new EventoHistorialResponse(
                documento.getFechaCarga(), "DOCUMENTO_CARGADO", null, null, aspirante, documento.getNombreArchivo());
    }
}
