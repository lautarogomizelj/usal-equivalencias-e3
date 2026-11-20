package com.usal.e3.service;

import java.time.LocalDate;
import java.util.List;

/**
 * Filtros de `GET /solicitudes` (contrato §6.2). `desde` y `hasta` son sobre la última actualización.
 * `sinResponsable` deja las que quien pregunta puede tomar: abiertas, sin responsable de su rol y con permiso.
 */
public record FiltroBandeja(
        List<String> estados,
        Integer carreraId,
        LocalDate desde,
        LocalDate hasta,
        String q,
        boolean asignadas,
        boolean sinResponsable,
        Boolean finalizadas) {}
