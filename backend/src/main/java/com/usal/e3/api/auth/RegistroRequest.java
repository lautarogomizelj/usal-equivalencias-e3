package com.usal.e3.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = Validaciones.REQUERIDO) @Email(message = Validaciones.EMAIL_INVALIDO) @Size(max = 255)
                String email,
        @NotBlank(message = Validaciones.REQUERIDO)
                @Pattern(regexp = Validaciones.PASSWORD, message = Validaciones.PASSWORD_INVALIDA)
                String password,
        @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String nombre,
        @NotBlank(message = Validaciones.REQUERIDO)
                @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_INVALIDO)
                String telefono,
        @NotBlank(message = Validaciones.REQUERIDO)
                @Pattern(regexp = Validaciones.DNI, message = Validaciones.DNI_INVALIDO)
                String dni) {}
