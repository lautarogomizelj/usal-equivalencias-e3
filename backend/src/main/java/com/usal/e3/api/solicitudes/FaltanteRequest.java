package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.NotBlank;

/** Documento requerido que el aspirante no cargó (RF-ACAD-009): tipo del catálogo y qué falta. */
public record FaltanteRequest(
        @NotBlank(message = Validaciones.REQUERIDO) String tipo,
        @NotBlank(message = Validaciones.REQUERIDO) String descripcion) {}
