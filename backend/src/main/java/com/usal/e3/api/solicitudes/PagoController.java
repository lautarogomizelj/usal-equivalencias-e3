package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.domain.ComprobantePago;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AnalisisService.Adjunto;
import com.usal.e3.service.PagoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Informe de materias, cupón y comprobantes de pago (contrato §6.6 y §7). */
@RestController
@RequestMapping("/solicitudes/{id}")
public class PagoController {

    /** Sin validaciones en el monto ni en el comentario: el servicio responde con un mensaje propio. */
    record CuponRequest(@NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado, BigDecimal monto) {}

    record InformeResponse(int materiasReconocidas, String observaciones, BigDecimal montoAPagar) {}

    record CuponResponse(BigDecimal monto, OffsetDateTime fechaEmision, String urlDescarga) {}

    record ComprobanteResponse(
            Integer id,
            int version,
            String nombreArchivo,
            Integer tamanoBytes,
            OffsetDateTime fechaCarga,
            String estadoVerificacion,
            String comentarioSecretaria,
            String urlDescarga) {

        static ComprobanteResponse de(ComprobantePago c) {
            return new ComprobanteResponse(
                    c.getId(),
                    c.getVersion(),
                    c.getNombreArchivo(),
                    c.getTamanoBytes(),
                    c.getFechaCarga(),
                    c.getEstadoVerificacion().name(),
                    c.getComentarioSecretaria(),
                    "/solicitudes/" + c.getSolicitudId() + "/pago/comprobantes/" + c.getId() + "/archivo");
        }
    }

    record PagoResponse(CuponResponse cupon, List<ComprobanteResponse> comprobantes) {}

    private final PagoService pago;

    public PagoController(PagoService pago) {
        this.pago = pago;
    }

    @GetMapping("/informe")
    InformeResponse informe(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        PagoService.Informe informe = pago.informe(actual, id);
        return new InformeResponse(
                informe.informe().getMateriasReconocidas(), informe.informe().getObservaciones(), informe.montoAPagar());
    }

    @GetMapping("/pago")
    PagoResponse info(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        PagoService.Info info = pago.info(actual, id);
        return new PagoResponse(
                info.cupon() == null
                        ? null
                        : new CuponResponse(
                                info.cupon().getMonto(),
                                info.cupon().getFechaEmision(),
                                "/solicitudes/" + id + "/pago/cupon/archivo"),
                info.comprobantes().stream().map(ComprobanteResponse::de).toList());
    }

    @GetMapping("/pago/cupon/archivo")
    ResponseEntity<Resource> cupon(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        return DocumentosController.descarga(pago.cuponArchivo(actual, id));
    }

    @GetMapping("/pago/comprobantes/{comprobanteId}/archivo")
    ResponseEntity<Resource> comprobante(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, @PathVariable Integer comprobanteId) {
        return DocumentosController.descarga(pago.comprobanteArchivo(actual, id, comprobanteId));
    }

    @PostMapping("/pago/cupon")
    SolicitudDetalleResponse enviarCupon(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody CuponRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                pago.enviarCupon(actual, id, body.estadoEsperado(), body.monto(), request.getRemoteAddr()));
    }

    @PostMapping(path = "/pago/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SolicitudDetalleResponse enviarComprobante(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestParam(required = false) String estadoEsperado,
            @RequestPart(required = false) MultipartFile archivo,
            HttpServletRequest request)
            throws IOException {
        Adjunto adjunto = archivo == null || archivo.isEmpty()
                ? null
                : new Adjunto(archivo.getOriginalFilename(), archivo.getBytes());
        return SolicitudDetalleResponse.de(
                pago.enviarComprobante(actual, id, estadoEsperado, adjunto, request.getRemoteAddr()));
    }

    @PostMapping("/pago/aceptar")
    SolicitudDetalleResponse aceptar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudesController.RechazoRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                pago.aceptar(actual, id, body.estadoEsperado(), body.comentario(), request.getRemoteAddr()));
    }

    @PostMapping("/pago/rechazar")
    SolicitudDetalleResponse rechazar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudesController.RechazoRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(
                pago.rechazar(actual, id, body.estadoEsperado(), body.comentario(), request.getRemoteAddr()));
    }
}
