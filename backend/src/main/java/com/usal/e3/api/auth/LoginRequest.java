package com.usal.e3.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = Validaciones.REQUERIDO) @Email(message = Validaciones.EMAIL_INVALIDO) String email,
        @NotBlank(message = Validaciones.REQUERIDO) String password,
        boolean recordarme) {}
