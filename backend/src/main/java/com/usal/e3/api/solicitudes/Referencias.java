package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Usuario;
import java.time.OffsetDateTime;

/** Referencias cortas que se repiten en `SolicitudResumen` y `SolicitudDetalle` (contrato §6.2 y §6.4). */
public final class Referencias {

    private Referencias() {}

    public record EstadoRef(String codigo, String nombre, boolean esTerminal) {

        static EstadoRef de(Estado estado) {
            return new EstadoRef(estado.getCodigo(), estado.getNombre(), estado.isEsTerminal());
        }
    }

    public record CarreraRef(Integer id, String codigo, String nombre) {

        static CarreraRef de(Carrera carrera) {
            return new CarreraRef(carrera.getId(), carrera.getCodigo(), carrera.getNombre());
        }
    }

    public record ResponsableRef(Integer id, String nombre, String email, OffsetDateTime fechaAsignacion) {

        static ResponsableRef de(Usuario usuario, OffsetDateTime fechaAsignacion) {
            return usuario == null
                    ? null
                    : new ResponsableRef(usuario.getId(), usuario.getNombre(), usuario.getEmail(), fechaAsignacion);
        }
    }
}
