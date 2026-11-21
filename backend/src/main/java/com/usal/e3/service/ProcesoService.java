package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lo que comparten Académico y Secretaría: bandeja (RF-PROC-001), tomar proceso (003) y darse de baja (004). */
@Service
public class ProcesoService {

    public static final String TOMAR_PROCESO = "TOMAR_PROCESO";
    public static final String DARSE_DE_BAJA = "DARSE_DE_BAJA";

    /** Los días de la bandeja y de las reuniones son días de Argentina, corra donde corra el servidor. */
    static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final SolicitudRepository solicitudes;
    private final CuentaService cuentas;
    private final PermisosService permisos;
    private final CatalogoService catalogo;
    private final AuditoriaService auditoria;
    private final NotificacionService notificaciones;
    private final SolicitudService detalles;

    public ProcesoService(
            SolicitudRepository solicitudes,
            CuentaService cuentas,
            PermisosService permisos,
            CatalogoService catalogo,
            AuditoriaService auditoria,
            NotificacionService notificaciones,
            SolicitudService detalles) {
        this.solicitudes = solicitudes;
        this.cuentas = cuentas;
        this.permisos = permisos;
        this.catalogo = catalogo;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
        this.detalles = detalles;
    }

    /**
     * El personal ve todas (acceso básico). Primero las carreras donde tiene permiso y después el resto, siempre
     * alfabético por carrera; dentro de cada carrera, la última actualización primero.
     */
    @Transactional(readOnly = true)
    public Page<ResumenSolicitud> bandeja(SesionActual actual, FiltroBandeja filtro, Pageable pagina) {
        Cuenta cuenta = cuentas.de(actual);
        Map<String, Estado> estados = catalogo.estadosPorCodigo();
        List<Carrera> conPermiso = permisos.carrerasConPermiso(cuenta);
        Set<Integer> misCarreras =
                conPermiso == null ? Set.of() : conPermiso.stream().map(Carrera::getId).collect(Collectors.toSet());
        Set<String> esperanAMiRol = Rol.ACADEMICO.equals(cuenta.rolCodigo())
                ? Estado.ESPERAN_AL_ACADEMICO
                : Rol.SECRETARIA.equals(cuenta.rolCodigo()) ? Estado.ESPERAN_A_SECRETARIA : Set.of();
        String busqueda = filtro.q() == null ? "" : filtro.q().strip().toLowerCase(Locale.ROOT);

        List<ResumenSolicitud> todas = solicitudes.findAll().stream()
                .filter(s -> filtro.estados() == null
                        || filtro.estados().isEmpty()
                        || filtro.estados().contains(s.getEstadoCodigo()))
                .filter(s -> filtro.carreraId() == null
                        || filtro.carreraId().equals(s.getCarreraDestino().getId()))
                .filter(s -> filtro.finalizadas() == null
                        || estados.get(s.getEstadoCodigo()).isEsTerminal() == filtro.finalizadas())
                .filter(s -> !filtro.asignadas() || s.tieneComoResponsable(cuenta))
                .filter(s -> !filtro.sinResponsable()
                        || (s.responsableDe(cuenta.rolCodigo()) == null
                                && !estados.get(s.getEstadoCodigo()).isEsTerminal()
                                && permisos.esParticipante(cuenta, s)))
                .filter(s -> enRango(s.getFechaUltimaMod(), filtro.desde(), filtro.hasta()))
                .filter(s -> busqueda.isEmpty() || coincide(s, busqueda))
                .map(s -> new ResumenSolicitud(
                        s,
                        estados.get(s.getEstadoCodigo()),
                        s.tieneComoResponsable(cuenta) && esperanAMiRol.contains(s.getEstadoCodigo()),
                        misCarreras.contains(s.getCarreraDestino().getId())))
                .sorted(Comparator.comparing((ResumenSolicitud r) -> !r.esDeMisCarreras())
                        .thenComparing(r -> r.solicitud().getCarreraDestino().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(r -> r.solicitud().getFechaUltimaMod(), Comparator.reverseOrder()))
                .toList();
        int desde = (int) Math.min(pagina.getOffset(), todas.size());
        int hasta = Math.min(desde + pagina.getPageSize(), todas.size());
        return new PageImpl<>(todas.subList(desde, hasta), pagina, todas.size());
    }

    /** El estado no cambia. Con la fila bloqueada, de dos que lo toman a la vez solo queda uno. */
    @Transactional
    public DetalleSolicitud tomar(SesionActual actual, Integer id, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        if (!permisos.esParticipante(cuenta, solicitud)) {
            throw new ApiException(ErrorCode.NO_PARTICIPANTE);
        }
        if (solicitud.responsableDe(cuenta.rolCodigo()) != null) {
            throw new ApiException(ErrorCode.PROCESO_YA_TOMADO);
        }
        if (catalogo.estadosPorCodigo().get(solicitud.getEstadoCodigo()).isEsTerminal()) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        solicitud.asignarResponsable(cuenta.rolCodigo(), (Usuario) cuenta, OffsetDateTime.now());
        registrar(cuenta, "tomar_proceso", solicitud, ip);
        notificaciones.procesoTomado(solicitud, cuenta);
        return detalles.detalle(cuenta, solicitud);
    }

    /** El proceso vuelve a quedar disponible para los demás de su rol con acceso a la carrera. */
    @Transactional
    public DetalleSolicitud darseDeBaja(SesionActual actual, Integer id, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        if (!solicitud.tieneComoResponsable(cuenta)) {
            throw new ApiException(ErrorCode.ACCESO_DENEGADO);
        }
        if (permisos.otrosConAcceso(cuenta, solicitud).isEmpty()) {
            throw new ApiException(ErrorCode.UNICO_RESPONSABLE);
        }
        solicitud.asignarResponsable(cuenta.rolCodigo(), null, OffsetDateTime.now());
        registrar(cuenta, "baja_responsable", solicitud, ip);
        notificaciones.procesoLiberado(solicitud, cuenta);
        return detalles.detalle(cuenta, solicitud);
    }

    private Solicitud paraActualizar(Integer id) {
        return solicitudes.buscarParaActualizar(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    /** El nombre va en los detalles para armar el historial sin volver a buscar al usuario. */
    private void registrar(Cuenta cuenta, String accion, Solicitud solicitud, String ip) {
        auditoria.registrarEn(
                cuenta,
                accion,
                "solicitud",
                solicitud.getId(),
                Map.of("usuario", cuenta.nombreCompleto(), "rol", cuenta.rolCodigo()),
                ip);
    }

    static boolean enRango(OffsetDateTime fecha, LocalDate desde, LocalDate hasta) {
        LocalDate dia = fecha.atZoneSameInstant(ZONA).toLocalDate();
        return (desde == null || !dia.isBefore(desde)) && (hasta == null || !dia.isAfter(hasta));
    }

    private static boolean coincide(Solicitud solicitud, String busqueda) {
        return List.of(
                        solicitud.getCodigoExp(),
                        solicitud.getAspirante().nombreCompleto(),
                        solicitud.getCarreraDestino().getNombre())
                .stream()
                .anyMatch(texto -> texto.toLowerCase(Locale.ROOT).contains(busqueda));
    }
}
