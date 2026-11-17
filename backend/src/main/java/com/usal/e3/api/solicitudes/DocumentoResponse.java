package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.DocumentoSolicitud;
import java.time.OffsetDateTime;

/** `Documento` del contrato (§6.5). La ruta en disco no sale: se descarga por `urlDescarga`. */
public record DocumentoResponse(
        Integer id,
        String tipo,
        String descripcion,
        String nombreArchivo,
        Integer tamanoBytes,
        OffsetDateTime fechaCarga,
        int version,
        Integer reemplazaAId,
        boolean borrador,
        String estadoRevision,
        String etapaRevision,
        String comentarioRevision,
        String revisadoPor,
        OffsetDateTime fechaRevision,
        boolean esRequeridoFaltante,
        String urlDescarga) {

    static DocumentoResponse de(DocumentoSolicitud d) {
        return new DocumentoResponse(
                d.getId(),
                d.getTipo().name(),
                d.getDescripcion(),
                d.getNombreArchivo(),
                d.getTamanoBytes(),
                d.getFechaCarga(),
                d.getVersion(),
                d.getReemplazaAId(),
                d.isBorrador(),
                d.getEstadoRevision().name(),
                d.getEtapaRevision().name(),
                d.getComentarioRevision(),
                d.getRevisadoPor() == null ? null : d.getRevisadoPor().nombreCompleto(),
                d.getFechaRevision(),
                d.isEsRequeridoFaltante(),
                d.getRutaArchivo() == null
                        ? null
                        : "/solicitudes/" + d.getSolicitudId() + "/documentos/" + d.getId() + "/archivo");
    }
}
