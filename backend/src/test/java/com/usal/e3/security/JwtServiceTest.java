package com.usal.e3.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwt = new JwtService("secreto-de-prueba-con-mas-de-32-bytes-0123456789");

    @Test
    void elTokenFirmadoDevuelveLaSesion() {
        UUID sesion = UUID.randomUUID();

        assertThat(jwt.leer(jwt.firmar(sesion, OffsetDateTime.now().plusHours(1)))).contains(sesion);
    }

    @Test
    void unTokenFirmadoConOtraClaveNoVale() {
        JwtService otro = new JwtService("otro-secreto-de-prueba-con-mas-de-32-bytes-012345");
        String token = otro.firmar(UUID.randomUUID(), OffsetDateTime.now().plusHours(1));

        assertThat(jwt.leer(token)).isEmpty();
    }

    @Test
    void basuraNoVale() {
        assertThat(jwt.leer("no-es-un-jwt")).isEmpty();
    }
}
