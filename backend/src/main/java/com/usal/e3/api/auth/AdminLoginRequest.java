package com.usal.e3.api.auth;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = Validaciones.REQUERIDO) String usuario,
        @NotBlank(message = Validaciones.REQUERIDO) String password) {}
