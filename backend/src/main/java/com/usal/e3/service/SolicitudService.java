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
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.AuditoriaRepository;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.HistorialEstadoRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El trámite visto desde la solicitud: creación (RF-ASP-001), estado y responsables (RF-ASP-003, RF-PROC-002). */
@Service
public class SolicitudService {

    private static final List<String> CAMBIOS_DE_RESPONSABLE = List.of(
            "tomar_proceso", "baja_responsable", "asignar_responsable", "quitar_responsable");

    private final SolicitudRepository solicitudes;
    private final AspiranteRepository aspirantes;
    private final CarreraRepository carreras;
    private final HistorialEstadoRepository historial;
    private final AuditoriaRepository auditoria;
    private final CuentaService cuentas;
    private final PermisosService permisos;
    private final TransicionService transiciones;
    private final DocumentoService documentos;
    private final CatalogoService catalogo;
    private final ChatService chat;

    public SolicitudService(
            SolicitudRepository solicitudes,
            AspiranteRepository aspirantes,
            CarreraRepository carreras,
            HistorialEstadoRepository historial,
            AuditoriaRepository auditoria,
            CuentaService cuentas,
            PermisosService permisos,
            TransicionService transiciones,
            DocumentoService documentos,
            CatalogoService catalogo,
            ChatService chat) {
        this.solicitudes = solicitudes;
        this.aspirantes = aspirantes;
        this.carreras = carreras;
        this.historial = historial;
        this.auditoria = auditoria;
        this.cuentas = cuentas;
        this.permisos = permisos;
        this.transiciones = transiciones;
        this.documentos = documentos;
        this.catalogo = catalogo;
        this.chat = chat;
    }

    /** Nace en `SOL-INI`, sin responsable. El motor avisa por mail a quienes tienen acceso a la carrera. */
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
                        Estado.ESPERAN_AL_ASPIRANTE.contains(s.getEstadoCodigo()),
                        false))
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
        // Los faltantes que agrega el Académico no son una carga: no tienen archivo ni fecha.
        List<DocumentoSolicitud> enviados = documentos.visibles(cuenta, solicitud, true).stream()
                .filter(d -> !d.isBorrador() && d.tieneArchivo())
                .toList();
        return new HistorialSolicitud(
                solicitud,
                historial.findBySolicitudIdOrderByFechaAsignacionDescIdDesc(id),
                auditoria.findByEntidadAndEntidadIdAndAccionIn("solicitud", id, CAMBIOS_DE_RESPONSABLE),
                enviados);
    }

    /**
     * Transiciones 2, 4 y 20: `SOL-INI` → `DOC-REV` (RF-ASP-002), `DOC-RECH` → `DOC-REV` (RF-ASP-004) y
     * `LEG-RECH` → `LEG-REV` (RF-ASP-009).
     */
    @Transactional
    public DetalleSolicitud enviarDocumentacion(SesionActual actual, Integer id, String estadoEsperado, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, estadoEsperado, cuenta);
        if (Estado.SOL_INI.equals(transicion.origen())) {
            documentos.enviarBorradores(solicitud);
        } else {
            documentos.enviarRecarga(solicitud);
        }
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalle(cuenta, solicitud);
    }

    /**
     * Transiciones 16 y 17 (RF-ASP-009): en `INSC-ACEP` el aspirante responde si sus documentos están
     * legalizados. Con "sí" pasa a `LEG-REV`; con "no", a `LEG-PEND`. No carga archivos.
     */
    @Transactional
    public DetalleSolicitud indicarLegalizacion(
            SesionActual actual, Integer id, String estadoEsperado, Boolean legalizados, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.INDICAR_LEGALIZACION, estadoEsperado, cuenta);
        if (legalizados == null) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "legalizados", "Indicá si tus documentos están legalizados.");
        }
        if (legalizados) {
            documentos.iniciarLegalizacion(solicitud);
        } else {
            transicion = new Transicion(transicion.accion(), transicion.rolActor(), transicion.origen(), Estado.LEG_PEND);
        }
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalle(cuenta, solicitud);
    }

    /** Transición 18 (RF-ASP-009): `LEG-PEND` → `LEG-REV`, cuando el aspirante avisa que ya legalizó. */
    @Transactional
    public DetalleSolicitud avisarLegalizado(SesionActual actual, Integer id, String estadoEsperado, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.AVISAR_LEGALIZADO, estadoEsperado, cuenta);
        documentos.iniciarLegalizacion(solicitud);
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

    /** Lo usan también las acciones del personal, que devuelven el detalle ya actualizado. */
    DetalleSolicitud detalle(Cuenta cuenta, Solicitud solicitud) {
        HistorialEstado ultimaObservacion = historial
                .findBySolicitudIdOrderByFechaAsignacionDescIdDesc(solicitud.getId())
                .stream()
                .filter(cambio -> cambio.getComentarios() != null && cambio.getUsuarioUltimoCambio() != null)
                .findFirst()
                .orElse(null);
        Estado estado = catalogo.estadosPorCodigo().get(solicitud.getEstadoCodigo());
        List<String> acciones = new ArrayList<>(transiciones.accionesDisponibles(solicitud, cuenta));
        acciones.addAll(accionesSinCambioDeEstado(cuenta, solicitud, estado));
        return new DetalleSolicitud(
                solicitud,
                estado,
                relacion(cuenta, solicitud),
                acciones,
                ultimaObservacion,
                documentos.visibles(cuenta, solicitud, false),
                chat.puedeEscribir(cuenta, solicitud, estado),
                chat.noLeidos(cuenta, solicitud));
    }

    /** Contrato §7.2. Sin responsable de su rol, al participante solo le queda tomar el proceso. */
    private List<String> accionesSinCambioDeEstado(Cuenta cuenta, Solicitud solicitud, Estado estado) {
        List<String> acciones = new ArrayList<>();
        if (cuenta.tipo() != TitularTipo.USUARIO) {
            return acciones;
        }
        boolean actuaComoAcademico = (Rol.ACADEMICO.equals(cuenta.rolCodigo()) || Rol.ADMIN.equals(cuenta.rolCodigo()))
                && transiciones.puedeActuar(cuenta, solicitud);
        if (actuaComoAcademico
                && (Estado.DOC_REV.equals(estado.getCodigo()) || Estado.LEG_REV.equals(estado.getCodigo()))) {
            acciones.add(RevisionService.GUARDAR_MARCAS);
            acciones.add(RevisionService.AGREGAR_FALTANTE);
        }
        if (actuaComoAcademico && Estado.REU_PEND.equals(estado.getCodigo())) {
            acciones.add(ReunionService.CARGAR_ENLACE_MEET);
        }
        if (actuaComoAcademico && Estado.ANAL_POST.equals(estado.getCodigo())) {
            acciones.add(AnalisisService.ACTUALIZAR_ANALISIS);
        }
        if (solicitud.tieneComoResponsable(cuenta)) {
            if (!permisos.otrosConAcceso(cuenta, solicitud).isEmpty()) {
                acciones.add(ProcesoService.DARSE_DE_BAJA);
            }
        } else if (solicitud.responsableDe(cuenta.rolCodigo()) == null
                && !estado.isEsTerminal()
                && permisos.esParticipante(cuenta, solicitud)) {
            acciones.add(ProcesoService.TOMAR_PROCESO);
        }
        return acciones;
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
