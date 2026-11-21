package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

/** Fechas que propone el Académico: parte `datos` de `PROPONER_FECHAS` y body de `PROPONER_SEGUNDA_RONDA` (contrato §7.3). */
public record HorariosRequest(
        @NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado,
        @NotNull(message = Validaciones.REQUERIDO) List<@NotNull @Valid Horario> horarios) {

    public record Horario(@NotNull(message = Validaciones.REQUERIDO) OffsetDateTime inicio) {}

    List<OffsetDateTime> inicios() {
        return horarios.stream().map(Horario::inicio).toList();
    }
}
