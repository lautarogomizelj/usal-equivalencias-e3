package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.PermisoSolicitudRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Toda validación de permisos pasa por acá, con `revoked_at IS NULL` (Requisitos v12 §8). */
@Service
public class PermisosService {

    private final PermisoCarreraRepository permisosCarrera;
    private final PermisoSolicitudRepository permisosSolicitud;

    public PermisosService(PermisoCarreraRepository permisosCarrera, PermisoSolicitudRepository permisosSolicitud) {
        this.permisosCarrera = permisosCarrera;
        this.permisosSolicitud = permisosSolicitud;
    }

    /** Solo Académico y Secretaría trabajan por carrera: para el resto devuelve nulo (contrato §4.1). */
    public List<Carrera> carrerasConPermiso(Cuenta cuenta) {
        if (!trabajaPorCarrera(cuenta)) {
            return null;
        }
        return permisosCarrera.carrerasActivas(cuenta.getId());
    }

    /** RF-AUTH-005: permiso temporal vigente sobre la solicitud o permanente sobre su carrera. Si no, observador. */
    public boolean esParticipante(Cuenta cuenta, Solicitud solicitud) {
        if (!trabajaPorCarrera(cuenta)) {
            return false;
        }
        return permisosSolicitud.activo(cuenta.getId(), solicitud.getId(), OffsetDateTime.now())
                || permisosCarrera.activo(cuenta.getId(), solicitud.getCarreraDestino().getId());
    }

    private static boolean trabajaPorCarrera(Cuenta cuenta) {
        String rol = cuenta.rolCodigo();
        return Rol.ACADEMICO.equals(rol) || Rol.SECRETARIA.equals(rol);
    }
}
