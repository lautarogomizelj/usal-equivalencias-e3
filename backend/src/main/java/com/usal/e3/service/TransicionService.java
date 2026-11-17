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
 * Motor de transiciones (RF-ESTADO-001, contrato §7): valida rol, permiso de participante y `estadoEsperado`,
 * y registra cada cambio en `estados_solicitud` y en auditoría. Corre en la transacción de quien lo llama, que
 * trae la solicitud con `buscarParaActualizar`.
 */
@Service
public class TransicionService {

    public static final String ENVIAR_DOCUMENTACION = "ENVIAR_DOCUMENTACION";
    public static final String CANCELAR_SOLICITUD = "CANCELAR_SOLICITUD";

    /** Las transiciones ya implementadas: cada sprint suma las suyas. De acá salen las `accionesDisponibles`. */
    static final List<Transicion> TABLA = List.of(
            new Transicion(ENVIAR_DOCUMENTACION, Rol.ASPIRANTE, Estado.SOL_INI, Estado.DOC_REV),
            new Transicion(CANCELAR_SOLICITUD, Rol.ASPIRANTE, Estado.SOL_INI, Estado.SOL_CANC));

    private final HistorialEstadoRepository historial;
    private final PermisosService permisos;
    private final AuditoriaService auditoria;

    public TransicionService(HistorialEstadoRepository historial, PermisosService permisos, AuditoriaService auditoria) {
        this.historial = historial;
        this.permisos = permisos;
        this.auditoria = auditoria;
    }

    /** Transición 1: (inicio) → `SOL-INI`. */
    public void registrarCreacion(Solicitud solicitud, Cuenta aspirante, String ip) {
        registrar(solicitud, null, aspirante, ip);
    }

    /** Si el estado cambió desde que el usuario abrió la pantalla, responde `TRANSICION_INVALIDA` (§0.16.4). */
    public Transicion validar(Solicitud solicitud, String accion, String estadoEsperado, Cuenta actor) {
        List<Transicion> delRol = TABLA.stream()
                .filter(t -> t.accion().equals(accion) && t.rolActor().equals(actor.rolCodigo()))
                .toList();
        if (delRol.isEmpty()) {
            throw new ApiException(ErrorCode.ACCESO_DENEGADO);
        }
        if (!puedeActuar(actor, solicitud)) {
            throw new ApiException(ErrorCode.NO_PARTICIPANTE);
        }
        String actual = solicitud.getEstadoCodigo();
        return delRol.stream()
                .filter(t -> t.origen().equals(actual) && actual.equals(estadoEsperado))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.TRANSICION_INVALIDA));
    }

    public void aplicar(Solicitud solicitud, Transicion transicion, Cuenta actor, String ip) {
        String anterior = solicitud.getEstadoCodigo();
        solicitud.cambiarEstado(transicion.destino(), OffsetDateTime.now());
        registrar(solicitud, anterior, actor, ip);
    }

    public List<String> accionesDisponibles(Solicitud solicitud, Cuenta cuenta) {
        List<String> acciones = TABLA.stream()
                .filter(t -> t.origen().equals(solicitud.getEstadoCodigo()) && t.rolActor().equals(cuenta.rolCodigo()))
                .map(Transicion::accion)
                .distinct()
                .toList();
        return acciones.isEmpty() || !puedeActuar(cuenta, solicitud) ? List.of() : acciones;
    }

    /** El aspirante actúa sobre las suyas; el personal, donde es participante (RF-AUTH-005). */
    private boolean puedeActuar(Cuenta cuenta, Solicitud solicitud) {
        return cuenta.tipo() == TitularTipo.ASPIRANTE
                ? solicitud.esPropiaDe(cuenta)
                : permisos.esParticipante(cuenta, solicitud);
    }

    private void registrar(Solicitud solicitud, String anterior, Cuenta actor, String ip) {
        Usuario usuario = actor instanceof Usuario personal ? personal : null;
        historial.save(new HistorialEstado(
                solicitud.getId(),
                solicitud.getEstadoCodigo(),
                anterior,
                usuario,
                null,
                solicitud.getFechaUltimoCambioEstado()));
        auditoria.registrarCambioEstado(actor, solicitud.getId(), anterior, solicitud.getEstadoCodigo(), null, ip);
    }
}
