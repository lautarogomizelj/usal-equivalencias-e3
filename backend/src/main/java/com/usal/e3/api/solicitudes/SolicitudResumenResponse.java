package com.usal.e3.api.solicitudes;

import com.usal.e3.api.solicitudes.Referencias.CarreraRef;
import com.usal.e3.api.solicitudes.Referencias.EstadoRef;
import com.usal.e3.api.solicitudes.Referencias.ResponsableRef;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.service.ResumenSolicitud;
import java.time.OffsetDateTime;

/** `SolicitudResumen` del contrato (§6.2). */
public record SolicitudResumenResponse(
        Integer id,
        String codigoExp,
        AspiranteRef aspirante,
        String universidadOrigen,
        CarreraRef carreraDestino,
        EstadoRef estado,
        ResponsableRef responsableAcademico,
        ResponsableRef responsableAdministrativo,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaUltimaMod,
        boolean requiereMiAccion,
        boolean esDeMisCarreras) {

    public record AspiranteRef(Integer id, String nombre) {}

    static SolicitudResumenResponse de(ResumenSolicitud resumen) {
        Solicitud s = resumen.solicitud();
        return new SolicitudResumenResponse(
                s.getId(),
                s.getCodigoExp(),
                new AspiranteRef(s.getAspirante().getId(), s.getAspirante().getNombre()),
                s.getDatosOrigen().getUniversidad(),
                CarreraRef.de(s.getCarreraDestino()),
                EstadoRef.de(resumen.estado()),
                ResponsableRef.de(s.getResponsableAcademico(), s.getFechaAsignacionAcademico()),
                ResponsableRef.de(s.getResponsableAdministrativo(), s.getFechaAsignacionAdministrativo()),
                s.getFechaCreacion(),
                s.getFechaUltimaMod(),
                resumen.requiereMiAccion(),
                false);
    }
}
