package com.usal.e3.api.auth;

/** Mismas reglas y mensajes que `frontend/src/validations` (interfaces §0.4). */
public final class Validaciones {

    public static final String REQUERIDO = "Este campo es obligatorio.";
    public static final String EMAIL_INVALIDO = "Ingresá un email válido.";
    public static final String PASSWORD = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";
    public static final String PASSWORD_INVALIDA =
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
    public static final String DNI = "^\\d{7,8}$";
    public static final String DNI_INVALIDO = "Ingresá un DNI válido (7 u 8 dígitos, sin puntos).";
    public static final String TELEFONO = "^\\+?[\\d\\s-]{8,20}$";
    public static final String TELEFONO_INVALIDO = "Ingresá un teléfono válido.";
    public static final String CODIGO = "^\\d{6}$";
    public static final String CODIGO_INVALIDO = "El código debe tener 6 dígitos.";

    private Validaciones() {}

    /** Los emails se guardan en minúscula para que el `UNIQUE` no dependa de cómo se escriban. */
    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
