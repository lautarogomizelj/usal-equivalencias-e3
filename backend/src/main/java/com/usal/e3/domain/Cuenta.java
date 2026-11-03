package com.usal.e3.domain;

import java.time.OffsetDateTime;

/** Lo común entre `usuarios` y `aspirantes`: login, bloqueo por intentos y perfil. */
public interface Cuenta {

    TitularTipo tipo();

    Integer getId();

    String getEmail();

    void setEmail(String email);

    String getNombre();

    void setNombre(String nombre);

    String getTelefono();

    void setTelefono(String telefono);

    String getAvatar();

    void setAvatar(String avatar);

    String getDni();

    String rolCodigo();

    /** Académico, Secretaría y Revisor entran con Google y no tienen contraseña (RF-AUTH-003). */
    boolean esOAuth();

    boolean isEmailValidado();

    boolean isActivo();

    String getPasswordHash();

    void setPasswordHash(String passwordHash);

    int getIntentosLoginFallidos();

    void setIntentosLoginFallidos(int intentos);

    OffsetDateTime getBloqueadoHasta();

    void setBloqueadoHasta(OffsetDateTime bloqueadoHasta);

    OffsetDateTime getFechaUltimoIntentoFallido();

    void setFechaUltimoIntentoFallido(OffsetDateTime fecha);

    void setFechaUltimoLogin(OffsetDateTime fecha);
}
