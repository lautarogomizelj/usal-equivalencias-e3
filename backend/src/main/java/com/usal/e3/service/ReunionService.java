package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HorarioPropuesto;
import com.usal.e3.domain.Reunion;
import com.usal.e3.domain.RondaReunion;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.HorarioPropuestoRepository;
import com.usal.e3.repository.ReunionRepository;
import com.usal.e3.repository.RondaReunionRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * La reunión (RF-ASP-005, RF-ACAD-003 y RF-ACAD-004): las dos rondas que propone el Académico, la elección del
 * aspirante, la fecha pautada por chat, el enlace de Meet y el resultado.
 */
@Service
public class ReunionService {

    public static final String CARGAR_ENLACE_MEET = "CARGAR_ENLACE_MEET";

    private static final int MAX_HORARIOS = 7;
    private static final int PRIMERA_HORA = 10;
    private static final int ULTIMA_HORA = 17;
    private static final int DIAS_HACIA_ADELANTE = 30;
    private static final int MAX_TEXTO = 500;
    private static final int MAX_NOTAS = 2000;
    private static final String PREFIJO_MEET = "https://meet.google.com/";

    /** Una ronda con sus horarios, del más cercano al más lejano. */
    public record Ronda(RondaReunion ronda, List<HorarioPropuesto> horarios) {}

    /** Lo que necesita `ReunionInfo` (contrato §6.6). `notas` son las de la reunión y solo las recibe el personal. */
    public record Info(List<Ronda> rondas, Reunion reunion, String notas) {}

    private final SolicitudRepository solicitudes;
    private final RondaReunionRepository rondas;
    private final HorarioPropuestoRepository horarios;
    private final ReunionRepository reuniones;
    private final CuentaService cuentas;
    private final TransicionService transiciones;
    private final AuditoriaService auditoria;
    private final NotificacionService notificaciones;
    private final SolicitudService detalles;

    public ReunionService(
            SolicitudRepository solicitudes,
            RondaReunionRepository rondas,
            HorarioPropuestoRepository horarios,
            ReunionRepository reuniones,
            CuentaService cuentas,
            TransicionService transiciones,
            AuditoriaService auditoria,
            NotificacionService notificaciones,
            SolicitudService detalles) {
        this.solicitudes = solicitudes;
        this.rondas = rondas;
        this.horarios = horarios;
        this.reuniones = reuniones;
        this.cuentas = cuentas;
        this.transiciones = transiciones;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
        this.detalles = detalles;
    }

    @Transactional(readOnly = true)
    public Info info(SesionActual actual, Integer id) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.findById(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        return info(cuenta, solicitud);
    }

    /** Transición 7: `REU-FEC-ASP-1` → `REU-FEC-ACAD`. El motivo y la disponibilidad quedan en la ronda. */
    @Transactional
    public DetalleSolicitud solicitarOtrasFechas(
            SesionActual actual, Integer id, String estadoEsperado, String motivo, String disponibilidad, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.SOLICITAR_OTRAS_FECHAS, estadoEsperado, cuenta);
        String motivoLimpio = obligatorio(motivo, "motivo", "Contanos por qué no podés en esos horarios.");
        String disponibilidadLimpia =
                obligatorio(disponibilidad, "disponibilidad", "Indicá cuándo podés reunirte.");
        rondaVigente(solicitud).pedirOtrasFechas(motivoLimpio, disponibilidadLimpia);
        transiciones.aplicar(
                solicitud, transicion, cuenta, "Motivo: " + motivoLimpio + " · Puede: " + disponibilidadLimpia, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 8: `REU-FEC-ACAD` → `REU-FEC-ASP-2`. No existe una 3ª ronda. */
    @Transactional
    public DetalleSolicitud segundaRonda(
            SesionActual actual, Integer id, String estadoEsperado, List<OffsetDateTime> inicios, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.PROPONER_SEGUNDA_RONDA, estadoEsperado, cuenta);
        abrirRonda(solicitud, 2, cuenta, inicios);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 9: `REU-FEC-ASP-1` o `-2` → `REU-PEND`. El Académico no confirma: solo suma el enlace de Meet. */
    @Transactional
    public DetalleSolicitud elegirHorario(
            SesionActual actual, Integer id, String estadoEsperado, Integer horarioId, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ELEGIR_HORARIO, estadoEsperado, cuenta);
        HorarioPropuesto horario =
                horarios.findByRondaIdInOrderByInicioAsc(List.of(rondaVigente(solicitud).getId())).stream()
                        .filter(h -> h.getId().equals(horarioId))
                        .findFirst()
                        .orElseThrow(() -> ApiException.enCampo(
                                ErrorCode.VALIDACION, "horarioId", "Elegí uno de los horarios propuestos."));
        if (!horario.getInicio().isAfter(OffsetDateTime.now())) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "horarioId", "Ese horario ya pasó. Elegí otro.");
        }
        horario.elegir();
        reuniones.save(new Reunion(solicitud.getId(), horario));
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 10: `REU-FEC-ASP-2` → `REU-CHAT`. La fecha se pauta por el chat de la solicitud. */
    @Transactional
    public DetalleSolicitud rechazarRonda(
            SesionActual actual, Integer id, String estadoEsperado, String motivo, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.RECHAZAR_SEGUNDA_RONDA, estadoEsperado, cuenta);
        String motivoLimpio = texto(motivo, "motivo");
        rondaVigente(solicitud).rechazar();
        transiciones.aplicar(solicitud, transicion, cuenta, motivoLimpio, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** En `REU-PEND`: el estado no cambia y se le avisa al aspirante. Se puede corregir las veces que haga falta. */
    @Transactional
    public Info cargarEnlace(SesionActual actual, Integer id, String enlaceMeet, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        transiciones.exigirQuePuedaActuar(cuenta, solicitud);
        if (!Estado.REU_PEND.equals(solicitud.getEstadoCodigo())) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        String enlace = enlaceMeet.strip();
        if (!enlace.startsWith(PREFIJO_MEET) || enlace.length() > MAX_TEXTO || enlace.chars().anyMatch(Character::isWhitespace)) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "enlaceMeet", "Pegá el enlace de Google Meet (" + PREFIJO_MEET + "…).");
        }
        Reunion reunion = reunionVigente(solicitud);
        reunion.cargarEnlace(enlace);
        solicitud.modificada(OffsetDateTime.now());
        auditoria.registrarEn(cuenta, "enlace_meet", "solicitud", solicitud.getId(), Map.of("enlaceMeet", enlace), ip);
        notificaciones.enlaceDeMeet(solicitud, reunion);
        return info(cuenta, solicitud);
    }

    /** Transición 11: `REU-CHAT` → `REU-PEND`. La fecha la pautaron por chat, así que no sigue las franjas de las rondas. */
    @Transactional
    public DetalleSolicitud fijarFecha(
            SesionActual actual, Integer id, String estadoEsperado, OffsetDateTime inicio, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion = transiciones.validar(solicitud, TransicionService.FIJAR_FECHA, estadoEsperado, cuenta);
        if (inicio == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "inicio", "Indicá el día y la hora que pautaron.");
        }
        if (!inicio.isAfter(OffsetDateTime.now())) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "inicio", "La reunión tiene que ser a futuro.");
        }
        reuniones.save(new Reunion(solicitud.getId(), inicio));
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 12: `REU-PEND` → `REU-FEC-ACAD`. El Académico vuelve a proponer fechas (interfaces, Anexo A #7). */
    @Transactional
    public DetalleSolicitud marcarNoAsistio(
            SesionActual actual, Integer id, String estadoEsperado, String comentario, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.MARCAR_NO_ASISTIO, estadoEsperado, cuenta);
        String comentarioLimpio = texto(comentario, "comentario");
        reunionVigente(solicitud).cerrar(Reunion.Resultado.NO_ASISTIO, null, OffsetDateTime.now());
        transiciones.aplicar(solicitud, transicion, cuenta, comentarioLimpio, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 13: `REU-PEND` → `ANAL-POST`. Las notas son del Académico: no van al historial que ve el aspirante. */
    @Transactional
    public DetalleSolicitud finalizar(SesionActual actual, Integer id, String estadoEsperado, String notas, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(cuenta, id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.FINALIZAR_REUNION, estadoEsperado, cuenta);
        String notasLimpias = notas == null || notas.isBlank() ? null : notas.strip();
        if (notasLimpias != null && notasLimpias.length() > MAX_NOTAS) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "notas", "Máximo " + MAX_NOTAS + " caracteres.");
        }
        reunionVigente(solicitud).cerrar(Reunion.Resultado.REALIZADA, notasLimpias, OffsetDateTime.now());
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Hasta 7 franjas de 1 hora, de 10:00 a 18:00, desde mañana y dentro de los próximos 30 días (RF-ACAD-002). */
    void abrirRonda(Solicitud solicitud, int numero, Cuenta academico, List<OffsetDateTime> inicios) {
        if (inicios.isEmpty() || inicios.size() > MAX_HORARIOS) {
            throw horariosInvalidos("Proponé entre 1 y " + MAX_HORARIOS + " horarios.");
        }
        if (inicios.stream().map(OffsetDateTime::toInstant).distinct().count() != inicios.size()) {
            throw horariosInvalidos("Hay horarios repetidos.");
        }
        LocalDate hoy = LocalDate.now(ProcesoService.ZONA);
        for (OffsetDateTime inicio : inicios) {
            ZonedDateTime local = inicio.atZoneSameInstant(ProcesoService.ZONA);
            boolean enPunto = local.getMinute() == 0 && local.getSecond() == 0 && local.getNano() == 0;
            if (!enPunto || local.getHour() < PRIMERA_HORA || local.getHour() > ULTIMA_HORA) {
                throw horariosInvalidos("Cada horario dura 1 hora, empieza en punto y va entre las 10:00 y las 18:00.");
            }
            LocalDate dia = local.toLocalDate();
            if (!dia.isAfter(hoy) || dia.isAfter(hoy.plusDays(DIAS_HACIA_ADELANTE))) {
                throw horariosInvalidos(
                        "Las fechas van desde mañana hasta dentro de " + DIAS_HACIA_ADELANTE + " días.");
            }
        }
        RondaReunion ronda =
                rondas.save(new RondaReunion(solicitud.getId(), numero, academico.getId(), OffsetDateTime.now()));
        inicios.forEach(inicio -> horarios.save(new HorarioPropuesto(ronda.getId(), inicio)));
    }

    private Info info(Cuenta cuenta, Solicitud solicitud) {
        List<RondaReunion> deLaSolicitud = rondas.findBySolicitudIdOrderByIdAsc(solicitud.getId());
        List<HorarioPropuesto> propuestos = horarios.findByRondaIdInOrderByInicioAsc(
                deLaSolicitud.stream().map(RondaReunion::getId).toList());
        List<Ronda> conHorarios = deLaSolicitud.stream()
                .map(ronda -> new Ronda(
                        ronda,
                        propuestos.stream()
                                .filter(horario -> horario.getRondaId().equals(ronda.getId()))
                                .toList()))
                .toList();
        Reunion reunion = reuniones.findFirstBySolicitudIdOrderByIdDesc(solicitud.getId()).orElse(null);
        boolean conNotas = reunion != null && cuenta.tipo() == TitularTipo.USUARIO;
        return new Info(conHorarios, reunion, conNotas ? reunion.getNotas() : null);
    }

    private Reunion reunionVigente(Solicitud solicitud) {
        return reuniones.findFirstBySolicitudIdOrderByIdDesc(solicitud.getId()).orElseThrow();
    }

    private RondaReunion rondaVigente(Solicitud solicitud) {
        return rondas.findBySolicitudIdOrderByIdAsc(solicitud.getId()).stream()
                .max(Comparator.comparing(RondaReunion::getId))
                .orElseThrow();
    }

    private Solicitud paraActualizar(Cuenta cuenta, Integer id) {
        return solicitudes.buscarParaActualizar(id)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    private static ApiException horariosInvalidos(String mensaje) {
        return ApiException.enCampo(ErrorCode.VALIDACION, "horarios", mensaje);
    }

    private static String obligatorio(String texto, String campo, String mensaje) {
        String limpio = texto(texto, campo);
        if (limpio == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, campo, mensaje);
        }
        return limpio;
    }

    private static String texto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        if (texto.strip().length() > MAX_TEXTO) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, campo, "Máximo " + MAX_TEXTO + " caracteres.");
        }
        return texto.strip();
    }
}
