package com.usal.e3.api.solicitudes;

import com.usal.e3.security.SesionActual;
import com.usal.e3.service.ArchivoDescarga;
import com.usal.e3.service.DocumentoService;
import com.usal.e3.service.RevisionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Documentos de una solicitud (contrato §6.5). */
@RestController
@RequestMapping("/solicitudes/{id}/documentos")
public class DocumentosController {

    private final DocumentoService documentos;
    private final RevisionService revision;

    public DocumentosController(DocumentoService documentos, RevisionService revision) {
        this.documentos = documentos;
        this.revision = revision;
    }

    @GetMapping
    List<DocumentoResponse> listar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean versiones) {
        return documentos.listar(actual, id, versiones).stream().map(DocumentoResponse::de).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<DocumentoResponse> subir(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestPart MultipartFile archivo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Integer reemplazaAId,
            HttpServletRequest request)
            throws IOException {
        DocumentoResponse subido = DocumentoResponse.de(documentos.subir(
                actual, id, tipo, descripcion, reemplazaAId, archivo.getOriginalFilename(), archivo.getBytes()));
        return ResponseEntity.created(URI.create(request.getContextPath() + subido.urlDescarga()))
                .body(subido);
    }

    /** "Guardar marcas" (RF-ACAD-009): no cambia el estado y devuelve los documentos vigentes. */
    @PutMapping("/marcas")
    List<DocumentoResponse> guardarMarcas(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody MarcasRequest body,
            HttpServletRequest request) {
        return revision.guardarMarcas(actual, id, body.estadoEsperado(), body.aMarcas(), request.getRemoteAddr())
                .stream()
                .map(DocumentoResponse::de)
                .toList();
    }

    @PostMapping("/faltantes")
    @ResponseStatus(HttpStatus.CREATED)
    DocumentoResponse agregarFaltante(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody FaltanteRequest body,
            HttpServletRequest request) {
        return DocumentoResponse.de(
                revision.agregarFaltante(actual, id, body.tipo(), body.descripcion(), request.getRemoteAddr()));
    }

    @DeleteMapping("/{documentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void quitar(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, @PathVariable Integer documentoId) {
        documentos.quitar(actual, id, documentoId);
    }

    @GetMapping("/{documentoId}/archivo")
    ResponseEntity<Resource> archivo(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, @PathVariable Integer documentoId) {
        return descarga(documentos.archivo(actual, id, documentoId));
    }

    /** Descarga binaria con el nombre original del archivo (contrato §1). */
    static ResponseEntity<Resource> descarga(ArchivoDescarga descarga) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(descarga.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(descarga.nombre(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(descarga.recurso());
    }
}
