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
    public static final String PROPONER_FECHAS = "PROPONER_FECHAS";
    public static final String SOLICITAR_OTRAS_FECHAS = "SOLICITAR_OTRAS_FECHAS";
    public static final String PROPONER_SEGUNDA_RONDA = "PROPONER_SEGUNDA_RONDA";
    public static final String ELEGIR_HORARIO = "ELEGIR_HORARIO";
    public static final String RECHAZAR_SEGUNDA_RONDA = "RECHAZAR_SEGUNDA_RONDA";
    public static final String FIJAR_FECHA = "FIJAR_FECHA";
    public static final String MARCAR_NO_ASISTIO = "MARCAR_NO_ASISTIO";
    public static final String FINALIZAR_REUNION = "FINALIZAR_REUNION";
    public static final String ACEPTAR_INSCRIPCION = "ACEPTAR_INSCRIPCION";
    public static final String RECHAZAR_INSCRIPCION = "RECHAZAR_INSCRIPCION";
    public static final String INDICAR_LEGALIZACION = "INDICAR_LEGALIZACION";
    public static final String AVISAR_LEGALIZADO = "AVISAR_LEGALIZADO";
    public static final String ACEPTAR_LEGALIZADOS = "ACEPTAR_LEGALIZADOS";
    public static final String RECHAZAR_LEGALIZADOS = "RECHAZAR_LEGALIZADOS";
    public static final String ENVIAR_CUPON = "ENVIAR_CUPON";
    public static final String ENVIAR_COMPROBANTE = "ENVIAR_COMPROBANTE";
    public static final String ACEPTAR_PAGO = "ACEPTAR_PAGO";
    public static final String RECHAZAR_PAGO = "RECHAZAR_PAGO";

    /** Las transiciones ya implementadas: cada sprint suma las suyas. De acá salen las `accionesDisponibles`. */
    static final List<Transicion> TABLA = List.of(
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.SOL_INI, Estado.DOC_REV),
            new Transicion(CANCELAR_SOLICITUD, Rol.ASPIRANTE, Estado.SOL_INI, Estado.SOL_CANC),
            new Transicion(RECHAZAR_DOCUMENTACION, Rol.ACADEMICO, Estado.DOC_REV, Estado.DOC_RECH),
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.DOC_RECH, Estado.DOC_REV),
            new Transicion(ACEPTAR_DOCUMENTACION, Rol.ACADEMICO, Estado.DOC_REV, Estado.ANAL_PEND),
            new Transicion(PROPONER_FECHAS, Rol.ACADEMICO, Estado.ANAL_PEND, Estado.REU_FEC_ASP_1),
            new Transicion(SOLICITAR_OTRAS_FECHAS, Rol.ASPIRANTE, Estado.REU_FEC_ASP_1, Estado.REU_FEC_ACAD),
            new Transicion(PROPONER_SEGUNDA_RONDA, Rol.ACADEMICO, Estado.REU_FEC_ACAD, Estado.REU_FEC_ASP_2),
            new Transicion(ELEGIR_HORARIO, Rol.ASPIRANTE, Estado.REU_FEC_ASP_1, Estado.REU_PEND),
            new Transicion(ELEGIR_HORARIO, Rol.ASPIRANTE, Estado.REU_FEC_ASP_2, Estado.REU_PEND),
            new Transicion(RECHAZAR_SEGUNDA_RONDA, Rol.ASPIRANTE, Estado.REU_FEC_ASP_2, Estado.REU_CHAT),
            new Transicion(FIJAR_FECHA, Rol.ACADEMICO, Estado.REU_CHAT, Estado.REU_PEND),
            new Transicion(MARCAR_NO_ASISTIO, Rol.ACADEMICO, Estado.REU_PEND, Estado.REU_FEC_ACAD),
            new Transicion(FINALIZAR_REUNION, Rol.ACADEMICO, Estado.REU_PEND, Estado.ANAL_POST),
            new Transicion(ACEPTAR_INSCRIPCION, Rol.ACADEMICO, Estado.ANAL_POST, Estado.INSC_ACEP),
            new Transicion(RECHAZAR_INSCRIPCION, Rol.ACADEMICO, Estado.ANAL_POST, Estado.INSC_RECH),
            // La misma acción lleva a `LEG-REV` (sí) o a `LEG-PEND` (no): el destino lo elige quien la ejecuta.
            new Transicion(INDICAR_LEGALIZACION, Rol.ASPIRANTE, Estado.INSC_ACEP, Estado.LEG_REV),
            new Transicion(INDICAR_LEGALIZACION, Rol.ASPIRANTE, Estado.INSC_ACEP, Estado.LEG_PEND),
            new Transicion(AVISAR_LEGALIZADO, Rol.ASPIRANTE, Estado.LEG_PEND, Estado.LEG_REV),
            new Transicion(RECHAZAR_LEGALIZADOS, Rol.ACADEMICO, Estado.LEG_REV, Estado.LEG_RECH),
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.LEG_RECH, Estado.LEG_REV),
            new Transicion(ACEPTAR_LEGALIZADOS, Rol.ACADEMICO, Estado.LEG_REV, Estado.CUPON_SOL),
            new Transicion(ENVIAR_CUPON, Rol.SECRETARIA, Estado.CUPON_SOL, Estado.CUPON_PAG),
            new Transicion(ENVIAR_COMPROBANTE, Rol.ASPIRANTE, Estado.CUPON_PAG, Estado.PAG_CTRL),
            new Transicion(RECHAZAR_PAGO, Rol.SECRETARIA, Estado.PAG_CTRL, Estado.PAG_REENV),
            new Transicion(ENVIAR_COMPROBANTE, Rol.ASPIRANTE, Estado.PAG_REENV, Estado.PAG_CTRL),
            new Transicion(ACEPTAR_PAGO, Rol.SECRETARIA, Estado.PAG_CTRL, Estado.E3_INI));

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
