package com.usal.e3.api.solicitudes;

import com.usal.e3.security.SesionActual;
import com.usal.e3.service.ExpedienteService;
import com.usal.e3.service.FiltroBandeja;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exportación de expedientes en ZIP (RF-PROC-005, contrato §7.2). El aspirante no entra: lo corta `SecurityConfig`. */
@RestController
@RequestMapping("/solicitudes")
public class ExpedientesController {

    private final ExpedienteService expedientes;

    public ExpedientesController(ExpedienteService expedientes) {
        this.expedientes = expedientes;
    }

    @GetMapping("/{id}/expediente")
    ResponseEntity<Resource> individual(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, HttpServletRequest request) {
        return DocumentosController.descarga(expedientes.individual(actual, id, request.getRemoteAddr()));
    }

    /** Los mismos filtros que la bandeja (`GET /solicitudes`), sin paginar. */
    @GetMapping("/expedientes")
    ResponseEntity<Resource> multiple(
            @AuthenticationPrincipal SesionActual actual,
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) Integer carreraId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean asignadas,
            @RequestParam(defaultValue = "false") boolean sinResponsable,
            @RequestParam(required = false) Boolean finalizadas,
            HttpServletRequest request) {
        FiltroBandeja filtro =
                new FiltroBandeja(estado, carreraId, desde, hasta, q, asignadas, sinResponsable, finalizadas);
        return DocumentosController.descarga(expedientes.multiple(actual, filtro, request.getRemoteAddr()));
    }
}
