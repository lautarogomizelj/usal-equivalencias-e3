package com.usal.e3.service;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.HistorialEstadoRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El trámite visto desde la solicitud: creación (RF-ASP-001), estado y responsables (RF-ASP-003). */
@Service
public class SolicitudService {

    private final SolicitudRepository solicitudes;
    private final AspiranteRepository aspirantes;
    private final CarreraRepository carreras;
    private final HistorialEstadoRepository historial;
    private final CuentaService cuentas;
    private final PermisosService permisos;
    private final TransicionService transiciones;
    private final DocumentoService documentos;
    private final CatalogoService catalogo;

    public SolicitudService(
            SolicitudRepository solicitudes,
            AspiranteRepository aspirantes,
            CarreraRepository carreras,
            HistorialEstadoRepository historial,
            CuentaService cuentas,
            PermisosService permisos,
            TransicionService transiciones,
            DocumentoService documentos,
            CatalogoService catalogo) {
        this.solicitudes = solicitudes;
        this.aspirantes = aspirantes;
        this.carreras = carreras;
        this.historial = historial;
        this.cuentas = cuentas;
        this.permisos = permisos;
        this.transiciones = transiciones;
        this.documentos = documentos;
        this.catalogo = catalogo;
    }

    /** Nace en `SOL-INI`, sin responsable. El aviso por mail a la carrera llega con las notificaciones (Sprint 4). */
    @Transactional
    public DetalleSolicitud crear(SesionActual actual, String carreraDestinoCodigo, DatosOrigen origen, String ip) {
        Aspirante aspirante = aspirantes.findById(actual.id())
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        if (!aspirante.isEmailValidado()) {
            throw new ApiException(ErrorCode.EMAIL_NO_VALIDADO);
        }
        Carrera carrera = carreras.findByCodigo(carreraDestinoCodigo)
                .filter(Carrera::isActivo)
                .orElseThrow(() -> ApiException.enCampo(
                        ErrorCode.VALIDACION, "carreraDestinoCodigo", "Elegí una carrera del catálogo."));
        String codigoExp = String.format("EXP-%d-%03d", Year.now().getValue(), solicitudes.siguienteNumeroExp());
        Solicitud solicitud = solicitudes.save(new Solicitud(codigoExp, aspirante, carrera, origen));
        transiciones.registrarCreacion(solicitud, aspirante, ip);
        return detalle(aspirante, solicitud);
    }

    /** Las del aspirante: primero las que esperan su acción, después por última actualización (contrato §6.2). */
    @Transactional(readOnly = true)
    public Page<ResumenSolicitud> delAspirante(
            SesionActual actual, List<String> estados, Boolean finalizadas, Pageable pagina) {
        Map<String, Estado> catalogoEstados = catalogo.estadosPorCodigo();
        List<ResumenSolicitud> todas = solicitudes.findByAspiranteId(actual.id()).stream()
                .filter(s -> estados == null || estados.isEmpty() || estados.contains(s.getEstadoCodigo()))
                .map(s -> new ResumenSolicitud(
                        s,
                        catalogoEstados.get(s.getEstadoCodigo()),
                        Estado.ESPERAN_AL_ASPIRANTE.contains(s.getEstadoCodigo())))
                .filter(r -> finalizadas == null || r.estado().isEsTerminal() == finalizadas)
                .sorted(Comparator.comparing((ResumenSolicitud r) -> !r.requiereMiAccion())
                        .thenComparing(r -> r.solicitud().getFechaUltimaMod(), Comparator.reverseOrder()))
                .toList();
        int desde = (int) Math.min(pagina.getOffset(), todas.size());
        int hasta = Math.min(desde + pagina.getPageSize(), todas.size());
        return new PageImpl<>(todas.subList(desde, hasta), pagina, todas.size());
    }

    @Transactional(readOnly = true)
    public DetalleSolicitud detalle(SesionActual actual, Integer id) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.findById(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        return detalle(cuenta, solicitud);
    }

    @Transactional(readOnly = true)
    public HistorialSolicitud historial(SesionActual actual, Integer id) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.findById(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        List<DocumentoSolicitud> enviados = documentos.visibles(cuenta, solicitud, true).stream()
                .filter(d -> !d.isBorrador())
                .toList();
        return new HistorialSolicitud(
                solicitud, historial.findBySolicitudIdOrderByFechaAsignacionDescIdDesc(id), enviados);
    }

    /** Transición 2 (RF-ASP-002): `SOL-INI` → `DOC-REV`. */
    @Transactional
    public DetalleSolicitud enviarDocumentacion(SesionActual actual, Integer id, String estadoEsperado, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, estadoEsperado, cuenta);
        documentos.enviarBorradores(solicitud);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalle(cuenta, solicitud);
    }

    /** Transición 27 (RF-ASP-010): `SOL-INI` → `SOL-CANC`. Los borradores se eliminan. */
    @Transactional
    public DetalleSolicitud cancelar(SesionActual actual, Integer id, String estadoEsperado, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.CANCELAR_SOLICITUD, estadoEsperado, cuenta);
        documentos.descartarBorradores(solicitud);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalle(cuenta, solicitud);
    }

    private DetalleSolicitud detalle(Cuenta cuenta, Solicitud solicitud) {
        HistorialEstado ultimaObservacion = historial
                .findBySolicitudIdOrderByFechaAsignacionDescIdDesc(solicitud.getId())
                .stream()
                .filter(cambio -> cambio.getComentarios() != null && cambio.getUsuarioUltimoCambio() != null)
                .findFirst()
                .orElse(null);
        return new DetalleSolicitud(
                solicitud,
                catalogo.estadosPorCodigo().get(solicitud.getEstadoCodigo()),
                relacion(cuenta, solicitud),
                transiciones.accionesDisponibles(solicitud, cuenta),
                ultimaObservacion,
                documentos.visibles(cuenta, solicitud, false));
    }

    /** Resuelve el modo de las interfaces: acción o solo lectura (contrato §6.4). */
    private String relacion(Cuenta cuenta, Solicitud solicitud) {
        return switch (cuenta.rolCodigo()) {
            case Rol.ASPIRANTE -> "ASPIRANTE";
            case Rol.ADMIN -> "ADMIN";
            case Rol.REVISOR -> "REVISOR";
            default -> permisos.esParticipante(cuenta, solicitud) ? "PARTICIPANTE" : "OBSERVADOR";
        };
    }
}
