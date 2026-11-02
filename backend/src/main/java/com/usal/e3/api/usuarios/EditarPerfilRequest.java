package com.usal.e3.api.usuarios;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: un campo ausente no se toca. El avatar es la clave de la galería por género (`hombre-02`). */
public record EditarPerfilRequest(
        @Size(max = 255) @Pattern(regexp = ".*\\S.*", message = Validaciones.REQUERIDO) String nombre,
        @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_INVALIDO) String telefono,
        @Pattern(regexp = "^(hombre|mujer)-\\d{2}$", message = "Elegí un avatar de la galería.") String avatar) {}
