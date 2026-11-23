package com.usal.e3.service;

import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Solicitud;
import java.util.List;

/** Lo que necesita `SolicitudDetalle` (contrato §6.4), visto por quien pregunta. */
public record DetalleSolicitud(
        Solicitud solicitud,
        Estado estado,
        String miRelacion,
        List<String> accionesDisponibles,
        HistorialEstado ultimaObservacion,
        List<DocumentoSolicitud> documentos,
        boolean puedeEscribirChat,
        int mensajesNoLeidos) {}
