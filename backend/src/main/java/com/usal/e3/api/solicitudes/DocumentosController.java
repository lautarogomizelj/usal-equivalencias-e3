package com.usal.e3.api.solicitudes;

import com.usal.e3.security.SesionActual;
import com.usal.e3.service.ArchivoDescarga;
import com.usal.e3.service.DocumentoService;
import jakarta.servlet.http.HttpServletRequest;
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

    public DocumentosController(DocumentoService documentos) {
        this.documentos = documentos;
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
            @RequestParam String tipo,
            @RequestParam(required = false) String descripcion,
            HttpServletRequest request)
            throws IOException {
        DocumentoResponse subido = DocumentoResponse.de(documentos.subir(
                actual, id, tipo, descripcion, archivo.getOriginalFilename(), archivo.getBytes()));
        return ResponseEntity.created(URI.create(request.getContextPath() + subido.urlDescarga()))
                .body(subido);
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
        ArchivoDescarga descarga = documentos.archivo(actual, id, documentoId);
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
