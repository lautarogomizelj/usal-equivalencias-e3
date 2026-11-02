package com.usal.e3.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Formato de error del contrato de la API (§1), compatible con ApiErrorBody del frontend. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        Map<String, List<String>> errors,
        Integer intentosRestantes,
        OffsetDateTime bloqueadoHasta,
        OffsetDateTime timestamp,
        String path) {

    public static ErrorResponse of(ErrorCode code, String message, Map<String, List<String>> errors, String path) {
        return new ErrorResponse(
                code.status().value(), code.name(), message, errors, null, null, OffsetDateTime.now(), path);
    }

    static ErrorResponse of(ApiException ex, String path) {
        return new ErrorResponse(
                ex.code().status().value(),
                ex.code().name(),
                ex.getMessage(),
                ex.errors(),
                ex.intentosRestantes(),
                ex.bloqueadoHasta(),
                OffsetDateTime.now(),
                path);
    }
}
