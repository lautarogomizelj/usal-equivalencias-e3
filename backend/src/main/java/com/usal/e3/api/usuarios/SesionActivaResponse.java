package com.usal.e3.api.usuarios;

import com.usal.e3.domain.Sesion;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SesionActivaResponse(
        UUID id, String ip, String userAgent, OffsetDateTime inicio, OffsetDateTime ultimoUso, boolean actual) {

    static SesionActivaResponse de(Sesion sesion, UUID sesionActual) {
        return new SesionActivaResponse(
                sesion.getId(),
                sesion.getIp(),
                sesion.getUserAgent(),
                sesion.getInicio(),
                sesion.getUltimoUso(),
                sesion.getId().equals(sesionActual));
    }
}
