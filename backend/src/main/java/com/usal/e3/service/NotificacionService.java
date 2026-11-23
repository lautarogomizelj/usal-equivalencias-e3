package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HorarioPropuesto;
import com.usal.e3.domain.InformeMaterias;
import com.usal.e3.domain.Notificacion;
import com.usal.e3.domain.Reunion;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.RondaReunion;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.DocumentoRepository;
import com.usal.e3.repository.HorarioPropuestoRepository;
import com.usal.e3.repository.InformeMateriasRepository;
import com.usal.e3.repository.NotificacionRepository;
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
 * Cada aviso queda además en la campana de quien lo recibe (contrato §8.2).
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
    private final InformeMateriasRepository informes;
    private final NotificacionRepository campana;

    public NotificacionService(
            MailService mail,
            PermisosService permisos,
            DocumentoRepository documentos,
            RondaReunionRepository rondas,
            HorarioPropuestoRepository horarios,
            ReunionRepository reuniones,
            InformeMateriasRepository informes,
            NotificacionRepository campana) {
        this.informes = informes;
        this.campana = campana;
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
        Cuenta aspirante = solicitud.getAspirante();
        String cambio = (anterior == null ? "" : anterior) + ">" + solicitud.getEstadoCodigo();
        switch (cambio) {
            case ">" + Estado.SOL_INI -> conAccesoALaCarrera(solicitud).forEach(usuario -> avisar(
                    solicitud,
                    usuario,
                    "E3 " + exp + ": nueva solicitud",
                    "Nueva solicitud del aspirante " + solicitud.getAspirante().nombreCompleto()
                            + ". Ingresá al sistema para ver los detalles o tomar el proceso."));
            case Estado.SOL_INI + ">" + Estado.DOC_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentación pendiente de revisión",
                    "Nueva solicitud de equivalencias con documentación pendiente de revisión. ID: " + exp);
            case Estado.DOC_REV + ">" + Estado.DOC_RECH -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": observaciones en tu documentación",
                    "Observaciones en tu documentación. Motivo: " + comentario + ". Documentos a recargar: "
                            + aRecargar(solicitud) + ".");
            case Estado.DOC_RECH + ">" + Estado.DOC_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentación recargada",
                    "El aspirante recargó documentación. ID: " + exp);
            case Estado.DOC_REV + ">" + Estado.ANAL_PEND -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": tu documentación fue aceptada",
                    "Tu documentación fue aceptada. Tu solicitud está siendo analizada por nuestro equipo académico.");
            case Estado.ANAL_PEND + ">" + Estado.REU_FEC_ASP_1 -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": elegí un horario para la reunión",
                    "Elegí un horario para la reunión. Horarios propuestos: " + horariosPropuestos(solicitud)
                            + ". La matriz de equivalencias y el posible horario de cursada están en tu solicitud.");
            case Estado.REU_FEC_ASP_1 + ">" + Estado.REU_FEC_ACAD -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": el aspirante pidió otras fechas",
                    "El aspirante solicitó otras fechas para la reunión. " + comentario + " (ID: " + exp + ")");
            case Estado.REU_FEC_ACAD + ">" + Estado.REU_FEC_ASP_2 -> avisar(
                    solicitud,
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
                avisar(solicitud, aspirante, asunto, texto);
            }
            case Estado.REU_PEND + ">" + Estado.ANAL_POST -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": la reunión se realizó",
                    "La reunión se realizó. Tu solicitud está siendo analizada para definir tu inscripción.");
            case Estado.REU_PEND + ">" + Estado.REU_FEC_ACAD -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": no registramos tu asistencia a la reunión",
                    "No registramos tu asistencia a la reunión. El Académico va a proponer nuevas fechas. ID: " + exp);
            case Estado.ANAL_POST + ">" + Estado.INSC_ACEP -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": tu inscripción fue aceptada",
                    "Tu inscripción fue aceptada. Ingresá a tu cuenta e indicá si tus documentos están legalizados.");
            case Estado.ANAL_POST + ">" + Estado.INSC_RECH -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": tu solicitud no pudo ser aprobada",
                    "Lamentablemente tu solicitud no pudo ser aprobada. Motivo: " + comentario);
            case Estado.INSC_ACEP + ">" + Estado.LEG_PEND -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": legalizá tus documentos",
                    "Indicaste que tus documentos no están legalizados. Legalizalos y avisá desde tu solicitud"
                            + " cuando estén listos para que el Académico los analice.");
            case Estado.INSC_ACEP + ">" + Estado.LEG_REV, Estado.LEG_PEND + ">" + Estado.LEG_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentos legalizados para analizar",
                    "El aspirante indicó que sus documentos están legalizados. ID: " + exp);
            case Estado.LEG_REV + ">" + Estado.LEG_RECH -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": observaciones en tus documentos legalizados",
                    "Observaciones en tus documentos legalizados. Motivo: " + comentario
                            + ". Documentos a recargar: " + aRecargar(solicitud) + ".");
            case Estado.LEG_RECH + ">" + Estado.LEG_REV -> alAcademico(
                    solicitud,
                    "E3 " + exp + ": documentos legalizados recargados",
                    "El aspirante recargó sus documentos legalizados. ID: " + exp);
            case Estado.LEG_REV + ">" + Estado.CUPON_SOL -> {
                aSecretaria(
                        solicitud,
                        "E3 " + exp + ": solicitud lista para gestión de pago",
                        "Solicitud lista para gestión de pago. Generá y enviá el cupón. ID: " + exp);
                int materias = informes.findBySolicitudId(solicitud.getId())
                        .map(InformeMaterias::getMateriasReconocidas)
                        .orElseThrow();
                avisar(
                        solicitud,
                        aspirante,
                        "E3 " + exp + ": tus documentos legalizados fueron aceptados",
                        "Tus documentos legalizados fueron aceptados. Materias reconocidas: " + materias
                                + ". El detalle de equivalencias está en tu solicitud. Secretaría te va a enviar el"
                                + " cupón de pago.");
            }
            case Estado.CUPON_SOL + ">" + Estado.CUPON_PAG -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": tu cupón de pago está listo",
                    "Tu cupón está listo. Descargalo desde tu solicitud, hacé el pago y cargá el comprobante.");
            case Estado.CUPON_PAG + ">" + Estado.PAG_CTRL, Estado.PAG_REENV + ">" + Estado.PAG_CTRL -> aSecretaria(
                    solicitud,
                    "E3 " + exp + ": comprobante de pago para verificar",
                    "El aspirante cargó el comprobante de pago. ID: " + exp);
            case Estado.PAG_CTRL + ">" + Estado.PAG_REENV -> avisar(
                    solicitud,
                    aspirante,
                    "E3 " + exp + ": hay un problema con tu comprobante",
                    "Hay un problema con tu comprobante. " + comentario + " Cargá uno nuevo desde tu solicitud.");
            case Estado.PAG_CTRL + ">" + Estado.E3_INI -> avisar(
                    solicitud,
                    aspirante, "E3 " + exp + ": tu pago fue verificado", "Tu pago fue verificado.");
            default -> {}
        }
    }

    /** No es una transición: en `ANAL-POST` el Académico actualizó la matriz o el horario (RF-ACAD-004). */
    public void analisisActualizado(Solicitud solicitud, String documento) {
        avisar(
                solicitud,
                solicitud.getAspirante(),
                "E3 " + solicitud.getCodigoExp() + ": se actualizó el análisis de tu solicitud",
                "El Académico actualizó " + documento + " de tu solicitud. ID: " + solicitud.getCodigoExp());
    }

    /** No es una transición: el Académico cargó o corrigió el enlace en `REU-PEND` (RF-ACAD-003). */
    public void enlaceDeMeet(Solicitud solicitud, Reunion reunion) {
        avisar(
                solicitud,
                solicitud.getAspirante(),
                "E3 " + solicitud.getCodigoExp() + ": tu reunión fue confirmada",
                "Tu reunión fue confirmada para el " + FECHA_HORA.format(reunion.getInicio()) + ". Link: "
                        + reunion.getEnlaceMeet());
    }

    public void procesoTomado(Solicitud solicitud, Cuenta responsable) {
        String nombre = responsable.nombreCompleto();
        permisos.otrosConAcceso(responsable, solicitud).forEach(usuario -> avisar(
                solicitud,
                usuario,
                solicitud.getCodigoExp() + ": " + nombre + " ha tomado este proceso",
                nombre + " es ahora responsable. Ya no podés tomar este proceso."));
    }

    public void procesoLiberado(Solicitud solicitud, Cuenta anterior) {
        permisos.otrosConAcceso(anterior, solicitud).forEach(usuario -> avisar(
                solicitud,
                usuario,
                solicitud.getCodigoExp() + ": el proceso vuelve a estar disponible",
                anterior.nombreCompleto() + " se dio de baja como responsable. Podés tomar el proceso."));
    }

    /** RF-ADMIN-003: el permiso permanente deja tomar procesos de la carrera enseguida. */
    public void permisoAsignado(Usuario usuario, Carrera carrera) {
        avisar(
                null,
                usuario,
                "E3: nuevo acceso a " + carrera.getNombre(),
                "Se te ha asignado acceso a " + carrera.getNombre() + ". Ya podés tomar procesos de esa carrera.");
    }

    /** RF-ADMIN-005: el Admin asignó o reemplazó a un responsable. Avisa a quien entra, a quien sale y al aspirante. */
    public void responsableAsignado(Solicitud solicitud, Usuario nuevo, Usuario anterior) {
        String exp = solicitud.getCodigoExp();
        avisar(solicitud, nuevo, "E3 " + exp + ": te asignaron como responsable", "Has sido asignado a " + exp + ".");
        if (anterior != null) {
            avisar(solicitud, anterior, "E3 " + exp + ": ya no sos responsable", "Has sido reemplazado en " + exp + ".");
        }
        avisar(
                solicitud,
                solicitud.getAspirante(),
                "E3 " + exp + ": tu proceso fue reasignado",
                "Tu proceso ha sido reasignado a " + nuevo.nombreCompleto() + ".");
    }

    /**
     * RF-ADMIN-004: la revocación de un permiso dejó el proceso sin responsable. Avisa a los demás del rol con
     * acceso; si no queda nadie, al Admin que revocó.
     */
    public void procesoSinResponsable(Solicitud solicitud, Usuario anterior, Cuenta admin) {
        String exp = solicitud.getCodigoExp();
        List<Usuario> otros = permisos.otrosConAcceso(anterior, solicitud);
        if (otros.isEmpty()) {
            String papel = Rol.ACADEMICO.equals(anterior.rolCodigo()) ? "académico" : "administrativo";
            avisar(
                    solicitud,
                    admin,
                    "E3 " + exp + ": proceso sin responsable " + papel,
                    "Proceso " + exp + " sin responsable " + papel + ". Asigná a alguien desde la solicitud.");
            return;
        }
        otros.forEach(usuario -> avisar(
                solicitud,
                usuario,
                exp + ": el proceso vuelve a estar disponible",
                anterior.nombreCompleto() + " ya no es responsable. Podés tomar el proceso."));
    }

    /** El mail y la campana, juntos. `solicitud` es nula cuando el aviso no es sobre un trámite. */
    private void avisar(Solicitud solicitud, Cuenta destinatario, String asunto, String texto) {
        campana.save(new Notificacion(destinatario, solicitud == null ? null : solicitud.getId(), asunto, texto));
        mail.enviarAviso(destinatario.getEmail(), asunto, texto);
    }

    /** Al responsable académico; si todavía nadie tomó el proceso, a los Académicos con acceso a la carrera. */
    private void alAcademico(Solicitud solicitud, String asunto, String texto) {
        alResponsable(solicitud, Rol.ACADEMICO, asunto, texto);
    }

    /** Lo mismo para Secretaría, que puede no haber tomado el proceso todavía cuando llega la etapa de pago. */
    private void aSecretaria(Solicitud solicitud, String asunto, String texto) {
        alResponsable(solicitud, Rol.SECRETARIA, asunto, texto);
    }

    private void alResponsable(Solicitud solicitud, String rol, String asunto, String texto) {
        Usuario responsable = solicitud.responsableDe(rol);
        List<Usuario> destinatarios = responsable != null ? List.of(responsable) : permisos.conAcceso(solicitud, rol);
        destinatarios.forEach(usuario -> avisar(solicitud, usuario, asunto, texto));
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
