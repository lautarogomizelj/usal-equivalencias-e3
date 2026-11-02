package com.usal.e3.exception;

import org.springframework.http.HttpStatus;

/** Códigos de negocio del contrato de la API (§1). */
public enum ErrorCode {
    VALIDACION(HttpStatus.BAD_REQUEST, "Revisá los datos ingresados."),
    DUPLICADO(HttpStatus.CONFLICT, "El recurso ya existe."),
    CREDENCIALES_INVALIDAS(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos."),
    CUENTA_BLOQUEADA(HttpStatus.LOCKED, "Tu cuenta está bloqueada."),
    EMAIL_NO_VALIDADO(HttpStatus.FORBIDDEN, "Tenés que validar tu email."),
    CODIGO_INVALIDO(HttpStatus.BAD_REQUEST, "El código ingresado no es válido."),
    CODIGO_EXPIRADO(HttpStatus.BAD_REQUEST, "El código expiró."),
    USUARIO_NO_REGISTRADO(HttpStatus.FORBIDDEN, "Usuario no registrado en el sistema. Contacta al admin."),
    TRANSICION_INVALIDA(HttpStatus.CONFLICT, "Esta acción ya no está disponible porque el estado de la solicitud cambió."),
    NO_PARTICIPANTE(HttpStatus.FORBIDDEN, "No tenés permiso de proceso sobre esta solicitud."),
    PROCESO_YA_TOMADO(HttpStatus.CONFLICT, "Otro usuario tomó el proceso antes."),
    UNICO_RESPONSABLE(HttpStatus.CONFLICT, "No podés darte de baja. Sos el único responsable."),
    PROCESO_ACTIVO(HttpStatus.CONFLICT, "Proceso activo. Para revocar, asigne responsable a otro usuario primero."),
    SIN_PERMISO_CARRERA(HttpStatus.CONFLICT, "El usuario no tiene permiso sobre la carrera."),
    CHAT_DESHABILITADO(HttpStatus.FORBIDDEN, "El chat se habilita cuando alguien tome el proceso."),
    ARCHIVO_INVALIDO(HttpStatus.BAD_REQUEST, "Formato no permitido. Solo se aceptan archivos PDF o JPG."),
    LIMITE_TAMANO(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el tamaño permitido."),
    DOCUMENTO_BLOQUEADO(HttpStatus.CONFLICT, "El documento está correcto y no se puede reemplazar."),
    MARCAS_INCOMPLETAS(HttpStatus.CONFLICT, "Hace falta al menos un documento a recargar y ninguno pendiente."),
    NO_AUTENTICADO(HttpStatus.UNAUTHORIZED, "Tu sesión venció. Volvé a ingresar."),
    ACCESO_DENEGADO(HttpStatus.FORBIDDEN, "No tenés permiso para hacer esto."),
    NO_ENCONTRADO(HttpStatus.NOT_FOUND, "El recurso solicitado no existe."),
    ERROR_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error en el servidor. Intentá nuevamente más tarde.");

    private final HttpStatus status;
    private final String mensaje;

    ErrorCode(HttpStatus status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
    }

    public HttpStatus status() {
        return status;
    }

    public String mensaje() {
        return mensaje;
    }
}
