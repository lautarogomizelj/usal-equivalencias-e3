package com.usal.e3.service;

import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;

/** Una fila del listado o de la bandeja (contrato §6.2), vista por quien pregunta. */
public record ResumenSolicitud(
        Solicitud solicitud, Estado estado, boolean requiereMiAccion, boolean esDeMisCarreras) {}
