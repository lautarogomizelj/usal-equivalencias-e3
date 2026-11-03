package com.usal.e3.api.usuarios;

import com.usal.e3.domain.Sesion;
import java.time.Duration;
import java.time.OffsetDateTime;

public record AccesoResponse(OffsetDateTime fecha, String ip, long duracionMinutos) {

    static AccesoResponse de(Sesion sesion) {
        OffsetDateTime fin = sesion.getCerradaEn() != null ? sesion.getCerradaEn() : sesion.getUltimoUso();
        return new AccesoResponse(
                sesion.getInicio(), sesion.getIp(), Duration.between(sesion.getInicio(), fin).toMinutes());
    }
}
