package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.AnalisisAcademico;
import java.time.OffsetDateTime;

/** `Analisis` del contrato (§6.6): la versión vigente de la matriz o del horario de cursada. */
public record AnalisisResponse(
        Integer id,
        String tipo,
        int version,
        String nombreArchivo,
        String notas,
        OffsetDateTime fechaCarga,
        String subidoPor,
        String urlDescarga) {

    static AnalisisResponse de(AnalisisAcademico a) {
        return new AnalisisResponse(
                a.getId(),
                a.getTipo().name(),
                a.getVersion(),
                a.getNombreArchivo(),
                a.getNotas(),
                a.getFechaCarga(),
                a.getSubidoPor().nombreCompleto(),
                a.getRutaArchivo() == null
                        ? null
                        : "/solicitudes/" + a.getSolicitudId() + "/analisis/" + a.getId() + "/archivo");
    }
}
