package com.usal.e3.api.solicitudes;

import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AnalisisService;
import com.usal.e3.service.AnalisisService.Adjunto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Matriz de equivalencias y horario de cursada de una solicitud (contrato §6.6 y §7.3). */
@RestController
@RequestMapping("/solicitudes/{id}/analisis")
public class AnalisisController {

    private final AnalisisService analisis;

    public AnalisisController(AnalisisService analisis) {
        this.analisis = analisis;
    }

    @GetMapping
    List<AnalisisResponse> listar(@AuthenticationPrincipal SesionActual actual, @PathVariable Integer id) {
        return analisis.vigentes(actual, id).stream().map(AnalisisResponse::de).toList();
    }

    @GetMapping("/{analisisId}/archivo")
    ResponseEntity<Resource> archivo(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, @PathVariable Integer analisisId) {
        return DocumentosController.descarga(analisis.archivo(actual, id, analisisId));
    }

    /** `PROPONER_FECHAS`: la parte `datos` es JSON y lleva el estado esperado y los horarios. */
    @PostMapping(value = "/propuesta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SolicitudDetalleResponse proponer(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestPart(required = false) MultipartFile matrizArchivo,
            @RequestParam(required = false) String matrizNotas,
            @RequestPart(required = false) MultipartFile horarioArchivo,
            @RequestParam(required = false) String horarioNotas,
            @Valid @RequestPart HorariosRequest datos,
            HttpServletRequest request)
            throws IOException {
        return SolicitudDetalleResponse.de(analisis.proponer(
                actual,
                id,
                datos.estadoEsperado(),
                adjunto(matrizArchivo),
                matrizNotas,
                adjunto(horarioArchivo),
                horarioNotas,
                datos.inicios(),
                request.getRemoteAddr()));
    }

    private static Adjunto adjunto(MultipartFile archivo) throws IOException {
        return archivo == null || archivo.isEmpty()
                ? null
                : new Adjunto(archivo.getOriginalFilename(), archivo.getBytes());
    }
}
