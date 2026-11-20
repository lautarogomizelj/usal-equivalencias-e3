package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.HistorialEstadoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Motor de transiciones (RF-ESTADO-001, contrato §7): valida rol, participante, responsable y `estadoEsperado`,
 * registra cada cambio en `estados_solicitud` y en auditoría, y avisa por mail (RF-ESTADO-002). Corre en la
 * transacción de quien lo llama, que trae la solicitud con `buscarParaActualizar`.
 */
@Service
public class TransicionService {

    public static final String ENVIAR_DOCUMENTACION = "ENVIAR_DOCUMENTACION";
    public static final String CANCELAR_SOLICITUD = "CANCELAR_SOLICITUD";
    public static final String ACEPTAR_DOCUMENTACION = "ACEPTAR_DOCUMENTACION";
    public static final String RECHAZAR_DOCUMENTACION = "RECHAZAR_DOCUMENTACION";

    /** Las transiciones ya implementadas: cada sprint suma las suyas. De acá salen las `accionesDisponibles`. */
    static final List<Transicion> TABLA = List.of(
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.SOL_INI, Estado.DOC_REV),
            new Transicion(CANCELAR_SOLICITUD, Rol.ASPIRANTE, Estado.SOL_INI, Estado.SOL_CANC),
            new Transicion(RECHAZAR_DOCUMENTACION, Rol.ACADEMICO, Estado.DOC_REV, Estado.DOC_RECH),
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.DOC_RECH, Estado.DOC_REV),
            new Transicion(ACEPTAR_DOCUMENTACION, Rol.ACADEMICO, Estado.DOC_REV, Estado.ANAL_PEND));

    private final HistorialEstadoRepository historial;
    private final PermisosService permisos;
    private final AuditoriaService auditoria;
    private final NotificacionService notificaciones;

    public TransicionService(
            HistorialEstadoRepository historial,
            PermisosService permisos,
            AuditoriaService auditoria,
            NotificacionService notificaciones) {
        this.historial = historial;
        this.permisos = permisos;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
    }

    /** Transición 1: (inicio) → `SOL-INI`. */
    public void registrarCreacion(Solicitud solicitud, Cuenta aspirante, String ip) {
        registrar(solicitud, null, aspirante, null, ip);
    }

    /** Si el estado cambió desde que el usuario abrió la pantalla, responde `TRANSICION_INVALIDA` (§0.16.4). */
    public Transicion validar(Solicitud solicitud, String accion, String estadoEsperado, Cuenta actor) {
        List<Transicion> delRol = TABLA.stream()
                .filter(t -> t.accion().equals(accion) && laEjecuta(t, actor))
                .toList();
        if (delRol.isEmpty()) {
            throw new ApiException(ErrorCode.ACCESO_DENEGADO);
        }
        exigirQuePuedaActuar(actor, solicitud);
        String actual = solicitud.getEstadoCodigo();
        return delRol.stream()
                .filter(t -> t.origen().equals(actual) && actual.equals(estadoEsperado))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.TRANSICION_INVALIDA));
    }

    public void aplicar(Solicitud solicitud, Transicion transicion, Cuenta actor, String ip) {
        aplicar(solicitud, transicion, actor, null, ip);
    }

    /** `comentario` es la observación del personal: queda en el historial y la ve el aspirante. */
    public void aplicar(Solicitud solicitud, Transicion transicion, Cuenta actor, String comentario, String ip) {
        String anterior = solicitud.getEstadoCodigo();
        solicitud.cambiarEstado(transicion.destino(), OffsetDateTime.now());
        registrar(solicitud, anterior, actor, comentario, ip);
    }

    public List<String> accionesDisponibles(Solicitud solicitud, Cuenta cuenta) {
        List<String> acciones = TABLA.stream()
                .filter(t -> t.origen().equals(solicitud.getEstadoCodigo()) && laEjecuta(t, cuenta))
                .map(Transicion::accion)
                .distinct()
                .toList();
        return acciones.isEmpty() || !puedeActuar(cuenta, solicitud) ? List.of() : acciones;
    }

    public boolean puedeActuar(Cuenta cuenta, Solicitud solicitud) {
        return impedimento(cuenta, solicitud) == null;
    }

    /** 403 `NO_PARTICIPANTE` o `NO_RESPONSABLE`, en ese orden (contrato §7). */
    public void exigirQuePuedaActuar(Cuenta cuenta, Solicitud solicitud) {
        ErrorCode impedimento = impedimento(cuenta, solicitud);
        if (impedimento != null) {
            throw new ApiException(impedimento);
        }
    }

    /** El Admin ejecuta las transiciones del Académico y de Secretaría, no las del aspirante (interfaces §7.0). */
    private static boolean laEjecuta(Transicion transicion, Cuenta actor) {
        return transicion.rolActor().equals(actor.rolCodigo())
                || (Rol.ADMIN.equals(actor.rolCodigo()) && !Rol.ASPIRANTE.equals(transicion.rolActor()));
    }

    /**
     * El aspirante actúa sobre las suyas. El personal tiene que ser participante (RF-AUTH-005) y además el
     * responsable de su rol: sin responsable, lo único que puede hacer es tomar el proceso (interfaces §5.0).
     * El Admin no necesita ninguna de las dos cosas.
     */
    private ErrorCode impedimento(Cuenta cuenta, Solicitud solicitud) {
        if (Rol.ADMIN.equals(cuenta.rolCodigo())) {
            return null;
        }
        if (cuenta.tipo() == TitularTipo.ASPIRANTE) {
            return solicitud.esPropiaDe(cuenta) ? null : ErrorCode.NO_PARTICIPANTE;
        }
        if (!permisos.esParticipante(cuenta, solicitud)) {
            return ErrorCode.NO_PARTICIPANTE;
        }
        return solicitud.tieneComoResponsable(cuenta) ? null : ErrorCode.NO_RESPONSABLE;
    }

    private void registrar(Solicitud solicitud, String anterior, Cuenta actor, String comentario, String ip) {
        Usuario usuario = actor instanceof Usuario personal ? personal : null;
        historial.save(new HistorialEstado(
                solicitud.getId(),
                solicitud.getEstadoCodigo(),
                anterior,
                usuario,
                comentario,
                solicitud.getFechaUltimoCambioEstado()));
        auditoria.registrarCambioEstado(actor, solicitud.getId(), anterior, solicitud.getEstadoCodigo(), comentario, ip);
        notificaciones.cambioDeEstado(solicitud, anterior, comentario);
    }
}
