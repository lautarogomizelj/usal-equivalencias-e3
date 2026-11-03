package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.repository.PermisoCarreraRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** Toda validación de permisos pasa por acá, con `revoked_at IS NULL` (Requisitos v12 §8). */
@Service
public class PermisosService {

    private final PermisoCarreraRepository permisosCarrera;

    public PermisosService(PermisoCarreraRepository permisosCarrera) {
        this.permisosCarrera = permisosCarrera;
    }

    /** Solo Académico y Secretaría trabajan por carrera: para el resto devuelve nulo (contrato §4.1). */
    public List<Carrera> carrerasConPermiso(Cuenta cuenta) {
        String rol = cuenta.rolCodigo();
        if (!Rol.ACADEMICO.equals(rol) && !Rol.SECRETARIA.equals(rol)) {
            return null;
        }
        return permisosCarrera.carrerasActivas(cuenta.getId());
    }
}
