package com.usal.e3.service;

import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;

public record ResumenSolicitud(Solicitud solicitud, Estado estado, boolean requiereMiAccion) {}
