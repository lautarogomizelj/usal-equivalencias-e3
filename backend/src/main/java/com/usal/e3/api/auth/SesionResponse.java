package com.usal.e3.api.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.service.DatosSesion;
import java.time.OffsetDateTime;
import java.util.List;

/** `Sesion` del contrato (§4.1). */
public record SesionResponse(
        TitularTipo tipo,
        Integer id,
        String nombre,
        String email,
        String rol,
        String avatar,
        boolean emailValidado,
        boolean esOAuth,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<CarreraResumen> carrerasConPermiso,
        OffsetDateTime expiraEn) {

    public record CarreraResumen(Integer id, String codigo, String nombre) {

        static CarreraResumen de(Carrera carrera) {
            return new CarreraResumen(carrera.getId(), carrera.getCodigo(), carrera.getNombre());
        }
    }

    static SesionResponse de(DatosSesion datos) {
        Cuenta cuenta = datos.cuenta();
        List<CarreraResumen> carreras = datos.carreras() == null
                ? null
                : datos.carreras().stream().map(CarreraResumen::de).toList();
        return new SesionResponse(
                cuenta.tipo(),
                cuenta.getId(),
                cuenta.getNombre(),
                cuenta.getEmail(),
                cuenta.rolCodigo(),
                cuenta.getAvatar(),
                cuenta.isEmailValidado(),
                cuenta.esOAuth(),
                carreras,
                datos.expiraEn());
    }
}
