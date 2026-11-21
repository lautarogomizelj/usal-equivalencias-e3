package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.PermisoSolicitudRepository;
import com.usal.e3.repository.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Toda validación de permisos pasa por acá, con `revoked_at IS NULL` (Requisitos v12 §8). */
@Service
public class PermisosService {

    private final PermisoCarreraRepository permisosCarrera;
    private final PermisoSolicitudRepository permisosSolicitud;
    private final UsuarioRepository usuarios;

    public PermisosService(
            PermisoCarreraRepository permisosCarrera,
            PermisoSolicitudRepository permisosSolicitud,
            UsuarioRepository usuarios) {
        this.permisosCarrera = permisosCarrera;
        this.permisosSolicitud = permisosSolicitud;
        this.usuarios = usuarios;
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

    /** El personal de un rol que puede tomar el proceso por su permiso sobre la carrera. */
    public List<Usuario> conAcceso(Solicitud solicitud, String rolCodigo) {
        return usuarios.conPermisoEnCarrera(solicitud.getCarreraDestino().getId(), rolCodigo);
    }

    /** Los demás de su rol con acceso a la carrera: reciben los avisos y de ellos depende la baja (RF-PROC-004). */
    public List<Usuario> otrosConAcceso(Cuenta cuenta, Solicitud solicitud) {
        return conAcceso(solicitud, cuenta.rolCodigo()).stream()
                .filter(usuario -> !usuario.getId().equals(cuenta.getId()))
                .toList();
    }

    private static boolean trabajaPorCarrera(Cuenta cuenta) {
        String rol = cuenta.rolCodigo();
        return Rol.ACADEMICO.equals(rol) || Rol.SECRETARIA.equals(rol);
    }
}
