package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.DocumentoRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revisión de la documentación inicial (RF-ACAD-001) con marcas por documento (RF-ACAD-009). Solo actúan el
 * Académico responsable y el Admin, y solo en `DOC-REV`; la revisión de legalizados llega con su sprint.
 */
@Service
public class RevisionService {

    public static final String GUARDAR_MARCAS = "GUARDAR_MARCAS";
    public static final String AGREGAR_FALTANTE = "AGREGAR_FALTANTE";

    private static final int MAX_COMENTARIO = 500;

    /** Marca que llega del frontend para un documento vigente. */
    public record Marca(Integer documentoId, EstadoRevision estado, String comentario) {}

    private final SolicitudRepository solicitudes;
    private final DocumentoRepository documentos;
    private final CuentaService cuentas;
    private final TransicionService transiciones;
    private final AuditoriaService auditoria;
    private final SolicitudService detalles;

    public RevisionService(
            SolicitudRepository solicitudes,
            DocumentoRepository documentos,
            CuentaService cuentas,
            TransicionService transiciones,
            AuditoriaService auditoria,
            SolicitudService detalles) {
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.cuentas = cuentas;
        this.transiciones = transiciones;
        this.auditoria = auditoria;
        this.detalles = detalles;
    }

    /** "Guardar marcas": el estado no cambia. Devuelve los documentos vigentes. */
    @Transactional
    public List<DocumentoSolicitud> guardarMarcas(
            SesionActual actual, Integer id, String estadoEsperado, List<Marca> marcas, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = enRevision(cuenta, id, estadoEsperado);
        marcar(cuenta, solicitud, marcas, ip);
        return vigentes(solicitud);
    }

    /** Queda como ❌ sin archivo: el aspirante lo carga en la recarga. */
    @Transactional
    public DocumentoSolicitud agregarFaltante(
            SesionActual actual, Integer id, String tipo, String descripcion, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = enRevision(cuenta, id, Estado.DOC_REV);
        String descripcionLimpia = DocumentoService.descripcion(descripcion);
        if (descripcionLimpia == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "descripcion", "Indicá qué documento falta.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        DocumentoSolicitud faltante = documentos.save(DocumentoSolicitud.faltante(
                solicitud.getId(), DocumentoService.tipo(tipo), descripcionLimpia, (Usuario) cuenta, ahora));
        solicitud.modificada(ahora);
        auditoria.registrarEn(
                cuenta, "agregar_faltante", "documento", faltante.getId(), Map.of("solicitudId", solicitud.getId()), ip);
        return faltante;
    }

    /** Transición 5: `DOC-REV` → `ANAL-PEND`. Marcar es opcional. */
    @Transactional
    public DetalleSolicitud aceptar(
            SesionActual actual, Integer id, String estadoEsperado, List<Marca> marcas, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, estadoEsperado, cuenta);
        marcar(cuenta, solicitud, marcas, ip);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 3: `DOC-REV` → `DOC-RECH`. Exige el motivo, al menos un ❌ y ningún documento sin revisar. */
    @Transactional
    public DetalleSolicitud rechazar(
            SesionActual actual, Integer id, String estadoEsperado, String comentario, List<Marca> marcas, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.RECHAZAR_DOCUMENTACION, estadoEsperado, cuenta);
        String motivo = comentario(comentario, "comentario");
        if (motivo == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "comentario", "Indicá el motivo del rechazo.");
        }
        marcar(cuenta, solicitud, marcas, ip);
        List<DocumentoSolicitud> revisados = vigentes(solicitud);
        boolean hayARecargar = revisados.stream().anyMatch(d -> d.getEstadoRevision() == EstadoRevision.RECARGAR);
        boolean haySinRevisar = revisados.stream().anyMatch(d -> d.getEstadoRevision() == EstadoRevision.PENDIENTE);
        if (!hayARecargar || haySinRevisar) {
            throw new ApiException(ErrorCode.MARCAS_INCOMPLETAS);
        }
        transiciones.aplicar(solicitud, transicion, cuenta, motivo, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    private Solicitud enRevision(Cuenta cuenta, Integer id, String estadoEsperado) {
        Solicitud solicitud = paraActualizar(id);
        transiciones.exigirQuePuedaActuar(cuenta, solicitud);
        if (!Estado.DOC_REV.equals(solicitud.getEstadoCodigo()) || !Estado.DOC_REV.equals(estadoEsperado)) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        return solicitud;
    }

    /** Cada cambio de marca queda en auditoría con la anterior y la nueva (RF-ACAD-009). */
    private void marcar(Cuenta cuenta, Solicitud solicitud, List<Marca> marcas, String ip) {
        if (marcas == null || marcas.isEmpty()) {
            return;
        }
        Map<Integer, DocumentoSolicitud> porId =
                vigentes(solicitud).stream().collect(Collectors.toMap(DocumentoSolicitud::getId, Function.identity()));
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Marca marca : marcas) {
            DocumentoSolicitud documento = porId.get(marca.documentoId());
            if (documento == null) {
                throw ApiException.enCampo(
                        ErrorCode.VALIDACION, "marcas", "El documento " + marca.documentoId() + " no es de esta solicitud.");
            }
            if (!documento.tieneArchivo() && marca.estado() != EstadoRevision.RECARGAR) {
                throw ApiException.enCampo(
                        ErrorCode.VALIDACION, "marcas", "Un documento faltante solo puede quedar como \"Debe recargar\".");
            }
            String comentario = comentario(marca.comentario(), "marcas");
            EstadoRevision anterior = documento.getEstadoRevision();
            if (anterior == marca.estado() && Objects.equals(documento.getComentarioRevision(), comentario)) {
                continue;
            }
            documento.marcar(marca.estado(), comentario, (Usuario) cuenta, ahora);
            if (anterior != marca.estado()) {
                auditoria.registrarEn(
                        cuenta,
                        "marca_documento",
                        "documento",
                        documento.getId(),
                        Map.of(
                                "solicitudId", solicitud.getId(),
                                "anterior", anterior.name(),
                                "nueva", marca.estado().name()),
                        ip);
            }
        }
        solicitud.modificada(ahora);
    }

    private List<DocumentoSolicitud> vigentes(Solicitud solicitud) {
        return DocumentoService.vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId()));
    }

    private Solicitud paraActualizar(Integer id) {
        return solicitudes.buscarParaActualizar(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    private static String comentario(String comentario, String campo) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        if (comentario.strip().length() > MAX_COMENTARIO) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, campo, "Máximo " + MAX_COMENTARIO + " caracteres.");
        }
        return comentario.strip();
    }
}
