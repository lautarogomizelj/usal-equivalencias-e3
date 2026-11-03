package com.usal.e3.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 401 y 403 de Spring Security con el mismo formato de error que el resto de la API. */
@Component
public class ErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ErroresSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        responder(ErrorCode.NO_AUTENTICADO, request, response);
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        responder(ErrorCode.ACCESO_DENEGADO, request, response);
    }

    private void responder(ErrorCode code, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(), ErrorResponse.of(code, code.mensaje(), null, request.getRequestURI()));
    }
}
