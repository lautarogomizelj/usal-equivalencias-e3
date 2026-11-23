package com.usal.e3.api.solicitudes;

import com.usal.e3.api.solicitudes.Referencias.CarreraRef;
import com.usal.e3.api.solicitudes.Referencias.EstadoRef;
import com.usal.e3.api.solicitudes.Referencias.ResponsableRef;
import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.service.DetalleSolicitud;
import java.time.OffsetDateTime;
import java.util.List;

/** `SolicitudDetalle` del contrato (§6.4). */
public record SolicitudDetalleResponse(
        Integer id,
        String codigoExp,
        EstadoRef estado,
        AspiranteDatos aspirante,
        DatosOrigenResponse datosOrigen,
        CarreraRef carreraDestino,
        ResponsableRef responsableAcademico,
        ResponsableRef responsableAdministrativo,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaUltimaMod,
        OffsetDateTime fechaUltimoCambioEstado,
        String miRelacion,
        List<String> accionesDisponibles,
        Observacion ultimaObservacion,
        ResumenDocumentos resumenDocumentos,
        boolean chatHabilitado,
        boolean puedeEscribirChat,
        int mensajesNoLeidos) {

    public record AspiranteDatos(Integer id, String nombre, String email, String telefono, String dni) {}

    public record DatosOrigenResponse(
            String universidad, String facultad, String carrera, String condicionAcademica, String observaciones) {}

    public record Observacion(String comentario, String usuario, OffsetDateTime fecha) {}

    public record ResumenDocumentos(long ok, long recargar, long pendiente) {}

    public static SolicitudDetalleResponse de(DetalleSolicitud detalle) {
        Solicitud s = detalle.solicitud();
        Aspirante aspirante = s.getAspirante();
        DatosOrigen origen = s.getDatosOrigen();
        HistorialEstado observacion = detalle.ultimaObservacion();
        List<DocumentoSolicitud> documentos = detalle.documentos();
        return new SolicitudDetalleResponse(
                s.getId(),
                s.getCodigoExp(),
                EstadoRef.de(detalle.estado()),
                new AspiranteDatos(
                        aspirante.getId(),
                        aspirante.nombreCompleto(),
                        aspirante.getEmail(),
                        aspirante.getTelefono(),
                        aspirante.getDni()),
                new DatosOrigenResponse(
                        origen.getUniversidad(),
                        origen.getFacultad(),
                        origen.getCarrera(),
                        origen.getCondicionAcademica(),
                        origen.getObservaciones()),
                CarreraRef.de(s.getCarreraDestino()),
                ResponsableRef.de(s.getResponsableAcademico(), s.getFechaAsignacionAcademico()),
                ResponsableRef.de(s.getResponsableAdministrativo(), s.getFechaAsignacionAdministrativo()),
                s.getFechaCreacion(),
                s.getFechaUltimaMod(),
                s.getFechaUltimoCambioEstado(),
                detalle.miRelacion(),
                detalle.accionesDisponibles(),
                observacion == null
                        ? null
                        : new Observacion(
                                observacion.getComentarios(),
                                observacion.getUsuarioUltimoCambio().nombreCompleto(),
                                observacion.getFechaAsignacion()),
                new ResumenDocumentos(
                        contar(documentos, EstadoRevision.OK),
                        contar(documentos, EstadoRevision.RECARGAR),
                        contar(documentos, EstadoRevision.PENDIENTE)),
                // RF-MSG-001: el chat se habilita cuando alguien toma el proceso.
                s.getResponsableAcademico() != null || s.getResponsableAdministrativo() != null,
                detalle.puedeEscribirChat(),
                detalle.mensajesNoLeidos());
    }

    private static long contar(List<DocumentoSolicitud> documentos, EstadoRevision estado) {
        return documentos.stream().filter(d -> d.getEstadoRevision() == estado).count();
    }
}
