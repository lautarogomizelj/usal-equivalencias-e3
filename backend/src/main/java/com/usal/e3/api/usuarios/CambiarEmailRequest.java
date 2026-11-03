package com.usal.e3.api.usuarios;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CambiarEmailRequest(
        @NotBlank(message = Validaciones.REQUERIDO) @Email(message = Validaciones.EMAIL_INVALIDO) String emailNuevo,
        @NotBlank(message = Validaciones.REQUERIDO) String passwordActual) {}
