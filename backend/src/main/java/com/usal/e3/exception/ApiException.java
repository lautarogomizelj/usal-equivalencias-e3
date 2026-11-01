package com.usal.e3.exception;

/** Error de negocio que el handler global convierte al formato de error común. */
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code) {
        this(code, code.mensaje());
    }

    public ApiException(ErrorCode code, String mensaje) {
        super(mensaje);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
