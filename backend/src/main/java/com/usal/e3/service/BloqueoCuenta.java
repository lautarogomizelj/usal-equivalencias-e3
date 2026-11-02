package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import java.time.Duration;
import java.time.OffsetDateTime;

/** Bloqueo por intentos de login (RF-AUTH-002): 3 fallos consecutivos bloquean 6 horas. */
final class BloqueoCuenta {

    static final int MAX_INTENTOS = 3;
    static final Duration DURACION_LOGIN = Duration.ofHours(6);
    private static final String MENSAJE_LOGIN =
            "Tu cuenta está bloqueada por 6 horas. Contacta al admin si necesitas acceso urgente.";

    private BloqueoCuenta() {}

    /** Si sigue bloqueada corta el login; si el bloqueo o los fallos ya vencieron, los limpia. */
    static void verificar(Cuenta cuenta, OffsetDateTime ahora) {
        OffsetDateTime hasta = cuenta.getBloqueadoHasta();
        if (hasta != null && hasta.isAfter(ahora)) {
            throw ApiException.bloqueada(MENSAJE_LOGIN, hasta);
        }
        OffsetDateTime ultimoFallo = cuenta.getFechaUltimoIntentoFallido();
        if (hasta != null || (ultimoFallo != null && ultimoFallo.plus(DURACION_LOGIN).isBefore(ahora))) {
            limpiar(cuenta);
        }
    }

    /** Suma un fallo y devuelve el error que corresponde mostrar. */
    static ApiException registrarFallo(Cuenta cuenta, OffsetDateTime ahora) {
        int intentos = cuenta.getIntentosLoginFallidos() + 1;
        cuenta.setIntentosLoginFallidos(intentos);
        cuenta.setFechaUltimoIntentoFallido(ahora);
        if (intentos >= MAX_INTENTOS) {
            OffsetDateTime hasta = ahora.plus(DURACION_LOGIN);
            cuenta.setBloqueadoHasta(hasta);
            return ApiException.bloqueada(MENSAJE_LOGIN, hasta);
        }
        return ApiException.conIntentos(ErrorCode.CREDENCIALES_INVALIDAS, MAX_INTENTOS - intentos);
    }

    static void limpiar(Cuenta cuenta) {
        cuenta.setIntentosLoginFallidos(0);
        cuenta.setBloqueadoHasta(null);
        cuenta.setFechaUltimoIntentoFallido(null);
    }
}
