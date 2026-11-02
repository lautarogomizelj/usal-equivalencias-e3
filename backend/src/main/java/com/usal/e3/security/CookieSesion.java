package com.usal.e3.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Cookie HttpOnly y SameSite=Lax del contrato de la API (§1). */
@Component
public class CookieSesion {

    public static final String NOMBRE = "E3_SESION";

    private final boolean secure;

    public CookieSesion(@Value("${e3.sesion.cookie-secure}") boolean secure) {
        this.secure = secure;
    }

    public String crear(String token, Duration duracion) {
        return base(token).maxAge(duracion).build().toString();
    }

    public String borrar() {
        return base("").maxAge(0).build().toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOMBRE, valor).httpOnly(true).secure(secure).sameSite("Lax").path("/");
    }
}
