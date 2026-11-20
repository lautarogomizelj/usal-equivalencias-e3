package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class BloqueoCuentaTest {

    private final OffsetDateTime ahora = OffsetDateTime.now();
    private final Aspirante cuenta = new Aspirante("a@b.com", "hash", "A", "B", "1", "12345678");

    @Test
    void losDosPrimerosFallosInformanLosIntentosRestantes() {
        assertThat(BloqueoCuenta.registrarFallo(cuenta, ahora).intentosRestantes()).isEqualTo(2);
        assertThat(BloqueoCuenta.registrarFallo(cuenta, ahora).intentosRestantes()).isEqualTo(1);
        assertThat(cuenta.getBloqueadoHasta()).isNull();
    }

    @Test
    void elTercerFalloBloqueaSeisHoras() {
        BloqueoCuenta.registrarFallo(cuenta, ahora);
        BloqueoCuenta.registrarFallo(cuenta, ahora);

        ApiException error = BloqueoCuenta.registrarFallo(cuenta, ahora);

        assertThat(error.code()).isEqualTo(ErrorCode.CUENTA_BLOQUEADA);
        assertThat(cuenta.getBloqueadoHasta()).isEqualTo(ahora.plusHours(6));
    }

    @Test
    void unaCuentaBloqueadaNoPuedeIntentar() {
        cuenta.setBloqueadoHasta(ahora.plusMinutes(1));

        assertThatThrownBy(() -> BloqueoCuenta.verificar(cuenta, ahora))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.code()).isEqualTo(ErrorCode.CUENTA_BLOQUEADA);
                    assertThat(ex.bloqueadoHasta()).isEqualTo(ahora.plusMinutes(1));
                });
    }

    @Test
    void alVencerElBloqueoElContadorVuelveACero() {
        cuenta.setIntentosLoginFallidos(3);
        cuenta.setBloqueadoHasta(ahora.minusMinutes(1));

        BloqueoCuenta.verificar(cuenta, ahora);

        assertThat(cuenta.getIntentosLoginFallidos()).isZero();
        assertThat(cuenta.getBloqueadoHasta()).isNull();
    }

    @Test
    void elBloqueoPorCodigosNoSeInformaComoDeSeisHoras() {
        cuenta.setBloqueadoHasta(ahora.plusMinutes(15));

        assertThatThrownBy(() -> BloqueoCuenta.verificar(cuenta, ahora))
                .isInstanceOf(ApiException.class)
                .hasMessage(ErrorCode.CUENTA_BLOQUEADA.mensaje());

        cuenta.setIntentosLoginFallidos(3);
        assertThatThrownBy(() -> BloqueoCuenta.verificar(cuenta, ahora)).hasMessageContaining("6 horas");
    }

    @Test
    void alVencerElBloqueoPorCodigosLosFallosDeLoginSeConservan() {
        cuenta.setIntentosLoginFallidos(2);
        cuenta.setFechaUltimoIntentoFallido(ahora.minusHours(1));
        cuenta.setBloqueadoHasta(ahora.minusMinutes(1));

        BloqueoCuenta.verificar(cuenta, ahora);

        assertThat(cuenta.getIntentosLoginFallidos()).isEqualTo(2);
        assertThat(cuenta.getBloqueadoHasta()).isNull();
    }

    @Test
    void elContadorSeReiniciaALasSeisHorasDelUltimoFallo() {
        cuenta.setIntentosLoginFallidos(2);
        cuenta.setFechaUltimoIntentoFallido(ahora.minusHours(6).minusMinutes(1));

        BloqueoCuenta.verificar(cuenta, ahora);

        assertThat(cuenta.getIntentosLoginFallidos()).isZero();
    }

    @Test
    void unFalloRecienteConservaElContador() {
        cuenta.setIntentosLoginFallidos(2);
        cuenta.setFechaUltimoIntentoFallido(ahora.minusHours(5));

        BloqueoCuenta.verificar(cuenta, ahora);

        assertThat(cuenta.getIntentosLoginFallidos()).isEqualTo(2);
    }
}
