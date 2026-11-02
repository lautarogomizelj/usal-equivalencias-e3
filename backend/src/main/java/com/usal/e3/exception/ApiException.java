package com.usal.e3.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Error de negocio que el handler global convierte al formato de error común. */
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final Map<String, List<String>> errors;
    private final Integer intentosRestantes;
    private final OffsetDateTime bloqueadoHasta;

    public ApiException(ErrorCode code) {
        this(code, code.mensaje());
    }

    public ApiException(ErrorCode code, String mensaje) {
        this(code, mensaje, null, null, null);
    }

    private ApiException(
            ErrorCode code,
            String mensaje,
            Map<String, List<String>> errors,
            Integer intentosRestantes,
            OffsetDateTime bloqueadoHasta) {
        super(mensaje);
        this.code = code;
        this.errors = errors;
        this.intentosRestantes = intentosRestantes;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    /** Error atado a un campo del formulario: `errors.email`, `errors.dni`… */
    public static ApiException enCampo(ErrorCode code, String campo, String mensaje) {
        return new ApiException(code, code.mensaje(), Map.of(campo, List.of(mensaje)), null, null);
    }

    public static ApiException conIntentos(ErrorCode code, int intentosRestantes) {
        return new ApiException(code, code.mensaje(), null, intentosRestantes, null);
    }

    public static ApiException bloqueada(String mensaje, OffsetDateTime bloqueadoHasta) {
        return new ApiException(ErrorCode.CUENTA_BLOQUEADA, mensaje, null, null, bloqueadoHasta);
    }

    public ErrorCode code() {
        return code;
    }

    public Map<String, List<String>> errors() {
        return errors;
    }

    public Integer intentosRestantes() {
        return intentosRestantes;
    }

    public OffsetDateTime bloqueadoHasta() {
        return bloqueadoHasta;
    }
}
