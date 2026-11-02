package com.usal.e3.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ValidarCodigoRequest(
        @NotBlank(message = Validaciones.REQUERIDO) @Email(message = Validaciones.EMAIL_INVALIDO) String email,
        @NotBlank(message = Validaciones.REQUERIDO)
                @Pattern(regexp = Validaciones.CODIGO, message = Validaciones.CODIGO_INVALIDO)
                String codigo) {}
