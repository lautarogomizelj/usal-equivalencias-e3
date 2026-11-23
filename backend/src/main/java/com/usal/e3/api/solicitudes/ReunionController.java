package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.ReunionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Fechas de la reunión, enlace de Meet y resultado (contrato §6.6, §7.1 y §7.2). */
@RestController
@RequestMapping("/solicitudes/{id}/reunion")
public class ReunionController {

    /** Sin `@NotBlank` en motivo ni disponibilidad: el servicio responde con un mensaje propio de cada campo. */
    record OtrasFechasRequest(
            @NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, String motivo, String disponibilidad) {}

    record ElegirHorarioRequest(
            @NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado,
            @NotNull(message = Validaciones.REQUERIDO) Integer horarioId) {}

    record RechazarRondaRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, String motivo) {}

    record EnlaceMeetRequest(@NotBlank(message = Validaciones.REQUERIDO) String enlaceMeet) {}

    /** Sin `@NotNull` en `inicio`: el servicio responde con un mensaje propio. */
    record FijarFechaRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, OffsetDateTime inicio) {}

    record NoAsistioRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, String comentario) {}

    record FinalizarRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, String notas) {}

    private final ReunionService reuniones;

    public ReunionController(ReunionService reuniones) {
        this.reuniones = reuniones;
    }

    @GetMapping
    ReunionInfoResponse info(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        return ReunionInfoResponse.de(reuniones.info(actual, id));
    }

    @PostMapping("/solicitar-otras-fechas")
    SolicitudDetalleResponse solicitarOtrasFechas(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody OtrasFechasRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(reuniones.solicitarOtrasFechas(
                actual, id, body.estadoEsperado(), body.motivo(), body.disponibilidad(), request.getRemoteAddr()));
    }

    @PostMapping("/segunda-ronda")
    SolicitudDetalleResponse segundaRonda(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody HorariosRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                reuniones.segundaRonda(actual, id, body.estadoEsperado(), body.inicios(), request.getRemoteAddr()));
    }

    @PostMapping("/elegir-horario")
    SolicitudDetalleResponse elegirHorario(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody ElegirHorarioRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(reuniones.elegirHorario(
                actual, id, body.estadoEsperado(), body.horarioId(), request.getRemoteAddr()));
    }

    @PostMapping("/rechazar-ronda")
    SolicitudDetalleResponse rechazarRonda(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody RechazarRondaRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                reuniones.rechazarRonda(actual, id, body.estadoEsperado(), body.motivo(), request.getRemoteAddr()));
    }

    @PostMapping("/fijar-fecha")
    SolicitudDetalleResponse fijarFecha(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody FijarFechaRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                reuniones.fijarFecha(actual, id, body.estadoEsperado(), body.inicio(), request.getRemoteAddr()));
    }

    @PostMapping("/no-asistio")
    SolicitudDetalleResponse marcarNoAsistio(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody NoAsistioRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(reuniones.marcarNoAsistio(
                actual, id, body.estadoEsperado(), body.comentario(), request.getRemoteAddr()));
    }

    @PostMapping("/finalizar")
    SolicitudDetalleResponse finalizar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody FinalizarRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                reuniones.finalizar(actual, id, body.estadoEsperado(), body.notas(), request.getRemoteAddr()));
    }

    /** `CARGAR_ENLACE_MEET`: no cambia el estado y devuelve la reunión con el enlace. */
    @PutMapping("/enlace")
    ReunionInfoResponse cargarEnlace(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody EnlaceMeetRequest body,
            HttpServletRequest request) {
        return ReunionInfoResponse.de(
                reuniones.cargarEnlace(actual, id, body.enlaceMeet(), request.getRemoteAddr()));
    }
}
