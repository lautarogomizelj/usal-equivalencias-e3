package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.service.HistorialSolicitud;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** `EventoHistorial` del contrato (§6.4). Los cambios de responsable llegan con Tomar proceso (Sprint 4). */
public record EventoHistorialResponse(
        OffsetDateTime fecha, String tipo, String estadoAnterior, String estadoNuevo, String usuario, String comentario) {

    /** Un único timeline, el más reciente primero (RF-AUDIT-002). */
    static List<EventoHistorialResponse> de(HistorialSolicitud historial) {
        String aspirante = historial.solicitud().getAspirante().getNombre();
        Stream<EventoHistorialResponse> cambios = historial.cambios().stream().map(c -> cambio(c, aspirante));
        Stream<EventoHistorialResponse> documentos =
                historial.documentosCargados().stream().map(d -> documento(d, aspirante));
        return Stream.concat(cambios, documentos)
                .sorted(Comparator.comparing(EventoHistorialResponse::fecha).reversed())
                .toList();
    }

    private static EventoHistorialResponse cambio(HistorialEstado cambio, String aspirante) {
        String usuario = cambio.getUsuarioUltimoCambio() == null
                ? aspirante
                : cambio.getUsuarioUltimoCambio().getNombre();
        return new EventoHistorialResponse(
                cambio.getFechaAsignacion(),
                "CAMBIO_ESTADO",
                cambio.getEstadoAnteriorCodigo(),
                cambio.getEstadoCodigo(),
                usuario,
                cambio.getComentarios());
    }

    private static EventoHistorialResponse documento(DocumentoSolicitud documento, String aspirante) {
        return new EventoHistorialResponse(
                documento.getFechaCarga(), "DOCUMENTO_CARGADO", null, null, aspirante, documento.getNombreArchivo());
    }
}
