package com.usal.e3.api.usuarios;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarPasswordRequest(
        @NotBlank(message = Validaciones.REQUERIDO) String passwordActual,
        @NotBlank(message = Validaciones.REQUERIDO)
                @Pattern(regexp = Validaciones.PASSWORD, message = Validaciones.PASSWORD_INVALIDA)
                String passwordNueva) {}
