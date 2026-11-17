package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.NotBlank;

/** Body de las acciones sin datos propios: solo el estado que el usuario tenía en pantalla (contrato §1). */
public record AccionRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado) {}
