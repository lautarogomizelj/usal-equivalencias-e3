package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.DocumentoRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Mails de RF-ESTADO-002: un aviso al actor siguiente en cada cambio de estado, y a los demás del rol cuando
 * alguien toma el proceso o se da de baja (RF-PROC-003 y 004). Cada sprint suma las filas de sus transiciones.
 */
@Service
public class NotificacionService {

    private final MailService mail;
    private final PermisosService permisos;
    private final DocumentoRepository documentos;

    public NotificacionService(MailService mail, PermisosService permisos, DocumentoRepository documentos) {
        this.mail = mail;
        this.permisos = permisos;
        this.documentos = documentos;
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
            default -> {}
        }
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

    private String aRecargar(Solicitud solicitud) {
        return DocumentoService.vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).stream()
                .filter(documento -> documento.getEstadoRevision() == EstadoRevision.RECARGAR)
                .map(DocumentoSolicitud::titulo)
                .collect(Collectors.joining(", "));
    }
}
