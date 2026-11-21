package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HorarioPropuesto;
import com.usal.e3.domain.Reunion;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.RondaReunion;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.DocumentoRepository;
import com.usal.e3.repository.HorarioPropuestoRepository;
import com.usal.e3.repository.ReunionRepository;
import com.usal.e3.repository.RondaReunionRepository;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Mails de RF-ESTADO-002: un aviso al actor siguiente en cada cambio de estado, y a los demás del rol cuando
 * alguien toma el proceso o se da de baja (RF-PROC-003 y 004). Cada sprint suma las filas de sus transiciones.
 */
@Service
public class NotificacionService {

    /** Las fechas de reunión se escriben en hora de Argentina: "lunes 30/11 a las 10:00". */
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern(
                    "EEEE d/MM 'a las' HH:mm", new Locale("es", "AR"))
            .withZone(ProcesoService.ZONA);

    private final MailService mail;
    private final PermisosService permisos;
    private final DocumentoRepository documentos;
    private final RondaReunionRepository rondas;
    private final HorarioPropuestoRepository horarios;
    private final ReunionRepository reuniones;

    public NotificacionService(
            MailService mail,
            PermisosService permisos,
            DocumentoRepository documentos,
            RondaReunionRepository rondas,
            HorarioPropuestoRepository horarios,
            ReunionRepository reuniones) {
        this.mail = mail;
        this.permisos = permisos;
        this.documentos = documentos;
        this.rondas = rondas;
        this.horarios = horarios;
        this.reuniones = reuniones;
    }

    /** Las transiciones sin fila en la tabla de RF-ESTADO-002 (las "internas") no avisan a nadie. */
    public void cambioDeEstado(Solicitud solicitud, String anterior, String comentario) {
        String exp = solicitud.getCodigoExp();
        String aspirante = solicitud.getAspirante().getEmail();
        String cambio = (anterior == null ? "" : anterior) + ">" + solicitud.getEstadoCodigo();
        switch (cambio) {
            case ">" + Estado.SOL_INI -> conAccesoALaCarrera(solicitud).forEach(usuario -> mail.enviarAviso(
                    usuario.getEmail(),
                    "E3 " + exp + ": nueva solicitud",
                    "Nueva solicitud del aspirante " + solicitud.getAspirante().nombreCompleto()
                            + ". Ingresá al sistema para ver los detalles o tomar el proceso."));
            case Estado.SOL_INI + ">" + Estado.DOC_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentación pendiente de revisión",
                    "Nueva solicitud de equivalencias con documentación pendiente de revisión. ID: " + exp);
            case Estado.DOC_REV + ">" + Estado.DOC_RECH -> mail.enviarAviso(
                    aspirante,
                    "E3 " + exp + ": observaciones en tu documentación",
                    "Observaciones en tu documentación. Motivo: " + comentario + ". Documentos a recargar: "
                            + aRecargar(solicitud) + ".");
            case Estado.DOC_RECH + ">" + Estado.DOC_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentación recargada",
                    "El aspirante recargó documentación. ID: " + exp);
            case Estado.DOC_REV + ">" + Estado.ANAL_PEND -> mail.enviarAviso(
                    aspirante,
                    "E3 " + exp + ": tu documentación fue aceptada",
                    "Tu documentación fue aceptada. Tu solicitud está siendo analizada por nuestro equipo académico.");
            case Estado.ANAL_PEND + ">" + Estado.REU_FEC_ASP_1 -> mail.enviarAviso(
                    aspirante,
                    "E3 " + exp + ": elegí un horario para la reunión",
                    "Elegí un horario para la reunión. Horarios propuestos: " + horariosPropuestos(solicitud)
                            + ". La matriz de equivalencias y el posible horario de cursada están en tu solicitud.");
            case Estado.REU_FEC_ASP_1 + ">" + Estado.REU_FEC_ACAD -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": el aspirante pidió otras fechas",
                    "El aspirante solicitó otras fechas para la reunión. " + comentario + " (ID: " + exp + ")");
            case Estado.REU_FEC_ACAD + ">" + Estado.REU_FEC_ASP_2 -> mail.enviarAviso(
                    aspirante,
                    "E3 " + exp + ": segunda ronda de fechas",
                    "El Académico propuso una segunda ronda de fechas para la reunión. Horarios propuestos: "
                            + horariosPropuestos(solicitud) + ".");
            case Estado.REU_FEC_ASP_1 + ">" + Estado.REU_PEND, Estado.REU_FEC_ASP_2 + ">" + Estado.REU_PEND ->
                alAcademico(
                        solicitud,
                        "E3 " + exp + ": el aspirante eligió la fecha de la reunión",
                        "El aspirante eligió la fecha " + FECHA_HORA.format(reunionVigente(solicitud).getInicio())
                                + " para la reunión. Incluí el enlace de Meet. ID: " + exp);
            case Estado.REU_FEC_ASP_2 + ">" + Estado.REU_CHAT -> {
                String asunto = "E3 " + exp + ": la fecha de la reunión se pauta por chat";
                String texto = "La 2ª ronda de fechas fue rechazada. Pauten la fecha de la reunión por el chat de la"
                        + " solicitud. ID: " + exp;
                alAcademico(solicitud, asunto, texto);
                mail.enviarAviso(aspirante, asunto, texto);
            }
            default -> {}
        }
    }

    /** No es una transición: el Académico cargó o corrigió el enlace en `REU-PEND` (RF-ACAD-003). */
    public void enlaceDeMeet(Solicitud solicitud, Reunion reunion) {
        mail.enviarAviso(
                solicitud.getAspirante().getEmail(),
                "E3 " + solicitud.getCodigoExp() + ": tu reunión fue confirmada",
                "Tu reunión fue confirmada para el " + FECHA_HORA.format(reunion.getInicio()) + ". Link: "
                        + reunion.getEnlaceMeet());
    }

    public void procesoTomado(Solicitud solicitud, Cuenta responsable) {
        String nombre = responsable.nombreCompleto();
        permisos.otrosConAcceso(responsable, solicitud).forEach(usuario -> mail.enviarAviso(
                usuario.getEmail(),
                solicitud.getCodigoExp() + ": " + nombre + " ha tomado este proceso",
                nombre + " es ahora responsable. Ya no podés tomar este proceso."));
    }

    public void procesoLiberado(Solicitud solicitud, Cuenta anterior) {
        permisos.otrosConAcceso(anterior, solicitud).forEach(usuario -> mail.enviarAviso(
                usuario.getEmail(),
                solicitud.getCodigoExp() + ": el proceso vuelve a estar disponible",
                anterior.nombreCompleto() + " se dio de baja como responsable. Podés tomar el proceso."));
    }

    /** Al responsable académico; si todavía nadie tomó el proceso, a los Académicos con acceso a la carrera. */
    private void alAcademico(Solicitud solicitud, String asunto, String texto) {
        Usuario responsable = solicitud.getResponsableAcademico();
        List<Usuario> destinatarios =
                responsable != null ? List.of(responsable) : permisos.conAcceso(solicitud, Rol.ACADEMICO);
        destinatarios.forEach(usuario -> mail.enviarAviso(usuario.getEmail(), asunto, texto));
    }

    private List<Usuario> conAccesoALaCarrera(Solicitud solicitud) {
        return Stream.of(Rol.ACADEMICO, Rol.SECRETARIA)
                .flatMap(rol -> permisos.conAcceso(solicitud, rol).stream())
                .toList();
    }

    /** Los de la ronda que se acaba de proponer, que es la última. */
    private String horariosPropuestos(Solicitud solicitud) {
        Integer ronda = rondas.findBySolicitudIdOrderByIdAsc(solicitud.getId()).stream()
                .map(RondaReunion::getId)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        return horarios.findByRondaIdInOrderByInicioAsc(List.of(ronda)).stream()
                .map(HorarioPropuesto::getInicio)
                .map(FECHA_HORA::format)
                .collect(Collectors.joining(", "));
    }

    private Reunion reunionVigente(Solicitud solicitud) {
        return reuniones.findFirstBySolicitudIdOrderByIdDesc(solicitud.getId()).orElseThrow();
    }

    private String aRecargar(Solicitud solicitud) {
        return DocumentoService.vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).stream()
                .filter(documento -> documento.getEstadoRevision() == EstadoRevision.RECARGAR)
                .map(DocumentoSolicitud::titulo)
                .collect(Collectors.joining(", "));
    }
}
