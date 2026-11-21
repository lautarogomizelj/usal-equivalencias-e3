package com.usal.e3.service;

import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.EventoAuditoria;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Solicitud;
import java.util.List;

/** Insumos del timeline de RF-AUDIT-002: cambios de estado, cambios de responsable y documentos enviados. */
public record HistorialSolicitud(
        Solicitud solicitud,
        List<HistorialEstado> cambios,
        List<EventoAuditoria> cambiosDeResponsable,
        List<DocumentoSolicitud> documentosCargados) {}
