package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class ProcesoServiceTest {

    @Test
    void elRangoDeFechasDeLaBandejaSeMideEnLaHoraDeArgentina() {
        // Las 22:00 del 18 en Buenos Aires ya son el 19 en UTC.
        OffsetDateTime modificada = OffsetDateTime.parse("2026-11-19T01:00:00Z");
        LocalDate dia18 = LocalDate.of(2026, 11, 18);

        assertThat(ProcesoService.enRango(modificada, dia18, dia18)).isTrue();
        assertThat(ProcesoService.enRango(modificada, dia18.plusDays(1), null)).isFalse();
    }
}
