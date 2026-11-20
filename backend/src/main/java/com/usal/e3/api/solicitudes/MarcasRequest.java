package com.usal.e3.api.solicitudes;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.service.RevisionService.Marca;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Body de "Guardar marcas" y de aceptar o rechazar la documentación (contrato §7.3). */
public record MarcasRequest(
        @NotBlank(message = Validaciones.REQUERIDO) String estadoEsperado,
        String comentario,
        @Valid List<MarcaRequest> marcas) {

    public record MarcaRequest(
            @NotNull(message = Validaciones.REQUERIDO) Integer documentoId,
            @NotNull(message = Validaciones.REQUERIDO) EstadoRevision estadoRevision,
            String comentario) {}

    List<Marca> aMarcas() {
        return marcas == null
                ? List.of()
                : marcas.stream()
                        .map(m -> new Marca(m.documentoId(), m.estadoRevision(), m.comentario()))
                        .toList();
    }
}
