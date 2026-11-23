package com.usal.e3.api.chat;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.api.solicitudes.DocumentosController;
import com.usal.e3.api.solicitudes.PaginaResponse;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AnalisisService.Adjunto;
import com.usal.e3.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Chat de la solicitud (contrato §8.1). Qué rol puede escribir está en `SecurityConfig`. */
@RestController
@RequestMapping("/solicitudes/{id}/chat")
public class ChatController {

    private static final int MAX_TAMANO_PAGINA = 100;

    record LeidosRequest(@NotNull(message = Validaciones.REQUERIDO) Integer hastaMensajeId) {}

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    @GetMapping("/mensajes")
    PaginaResponse<MensajeResponse> mensajes(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int despuesDe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(chat.mensajes(actual, id, despuesDe, pagina), MensajeResponse::de);
    }

    @PostMapping(value = "/mensajes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    MensajeResponse enviar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestParam(required = false) String contenido,
            @RequestParam(required = false) String tipoConsulta,
            @RequestPart(required = false) MultipartFile adjunto)
            throws IOException {
        Adjunto archivo = adjunto == null || adjunto.isEmpty()
                ? null
                : new Adjunto(adjunto.getOriginalFilename(), adjunto.getBytes());
        return MensajeResponse.de(chat.enviar(actual, id, contenido, tipoConsulta, archivo));
    }

    @PostMapping("/leidos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarLeidos(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody LeidosRequest body) {
        chat.marcarLeidos(actual, id, body.hastaMensajeId());
    }

    @GetMapping("/mensajes/{mensajeId}/adjunto")
    ResponseEntity<Resource> adjunto(
            @AuthenticationPrincipal SesionActual actual, @PathVariable Integer id, @PathVariable Integer mensajeId) {
        return DocumentosController.descarga(chat.adjunto(actual, id, mensajeId));
    }
}
