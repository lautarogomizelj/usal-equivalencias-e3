package com.usal.e3.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> negocio(ApiException ex, HttpServletRequest request) {
        return responder(ex.code(), ex.getMessage(), null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));
        return responder(ErrorCode.VALIDACION, ErrorCode.VALIDACION.mensaje(), errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> cuerpoInvalido(HttpServletRequest request) {
        return responder(ErrorCode.VALIDACION, ErrorCode.VALIDACION.mensaje(), null, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> tamano(HttpServletRequest request) {
        return responder(ErrorCode.LIMITE_TAMANO, ErrorCode.LIMITE_TAMANO.mensaje(), null, request);
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    ResponseEntity<ErrorResponse> noEncontrado(HttpServletRequest request) {
        return responder(ErrorCode.NO_ENCONTRADO, ErrorCode.NO_ENCONTRADO.mensaje(), null, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(ErrorCode.ERROR_INTERNO, ErrorCode.ERROR_INTERNO.mensaje(), null, request);
    }

    private ResponseEntity<ErrorResponse> responder(
            ErrorCode code, String mensaje, Map<String, List<String>> errors, HttpServletRequest request) {
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code, mensaje, errors, request.getRequestURI()));
    }
}
