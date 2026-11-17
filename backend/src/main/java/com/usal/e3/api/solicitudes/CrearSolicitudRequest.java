package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario inicial (contrato §6.3). Los datos personales salen de la cuenta. */
public record CrearSolicitudRequest(
        @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String universidadOrigen,
        @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String facultadOrigen,
        @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String carreraOrigen,
        @NotBlank(message = Validaciones.REQUERIDO) String carreraDestinoCodigo,
        @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 50) String condicionAcademica,
        @Size(max = 2000) String observaciones) {}
