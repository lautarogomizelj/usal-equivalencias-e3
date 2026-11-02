package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** RF-AUTH-007 y la tabla de permisos del contrato (§4.2). */
class PerfilIntegrationTest extends IntegracionBase {

    private static final String EMAIL = "juan.perez@gmail.com";
    private static final String ADMIN = "admin@usal.edu.ar";

    @Test
    void elAspiranteVeSuPerfilYSusCamposEditables() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        mvc.perform(get("/perfil").cookie(loginAspirante(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("40123456"))
                .andExpect(jsonPath("$.rol").value("ASPIRANTE"))
                .andExpect(jsonPath("$.esOAuth").value(false))
                .andExpect(jsonPath("$.camposEditables.length()").value(3));
    }

    @Test
    void elAspiranteEditaSoloLoQueManda() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);

        enviar(patch("/perfil"), Map.of("telefono", "+54 11 4444-0000", "avatar", "hombre-02"), sesion)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("+54 11 4444-0000"))
                .andExpect(jsonPath("$.avatar").value("hombre-02"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));

        assertThat(filasAuditoria(EMAIL, "cambio_perfil")).isEqualTo(1);
    }

    @Test
    void unAvatarFueraDeLaGaleriaSeRechaza() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        enviar(patch("/perfil"), Map.of("avatar", "../../etc/passwd"), loginAspirante(EMAIL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.avatar").exists());
    }

    @Test
    void elPersonalConGoogleNoEditaSuNombre() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        Cookie sesion = loginDesarrollo("m.gomez@usal.edu.ar");

        enviar(patch("/perfil"), Map.of("nombre", "Otro"), sesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
        enviar(patch("/perfil"), Map.of("telefono", "+54 11 4444-0000"), sesion).andExpect(status().isOk());
    }

    @Test
    void elRevisorSoloLee() throws Exception {
        usuario("revisor@usal.edu.ar", "REVISOR");
        Cookie sesion = loginDesarrollo("revisor@usal.edu.ar");

        mvc.perform(get("/perfil").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.camposEditables.length()").value(0));
        enviar(patch("/perfil"), Map.of("telefono", "+54 11 4444-0000"), sesion).andExpect(status().isForbidden());
    }

    @Test
    void cambiarLaContrasenaExigeLaActualYCierraLasOtrasSesiones() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie estaSesion = loginAspirante(EMAIL);
        Cookie otraSesion = loginAspirante(EMAIL);

        enviar(put("/perfil/password"), Map.of("passwordActual", "Mala#1234", "passwordNueva", "Nueva#2026"), estaSesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.passwordActual").exists());
        enviar(put("/perfil/password"), Map.of("passwordActual", PASSWORD, "passwordNueva", "Nueva#2026"), estaSesion)
                .andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").cookie(estaSesion)).andExpect(status().isOk());
        mvc.perform(get("/auth/me").cookie(otraSesion)).andExpect(status().isUnauthorized());
        assertThat(filasAuditoria(EMAIL, "cambio_password")).isEqualTo(1);
    }

    @Test
    void elPersonalConGoogleNoTieneContrasena() throws Exception {
        usuario("m.gomez@usal.edu.ar", "SECRE");

        enviar(
                        put("/perfil/password"),
                        Map.of("passwordActual", PASSWORD, "passwordNueva", "Nueva#2026"),
                        loginDesarrollo("m.gomez@usal.edu.ar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdminCambiaSuEmailDentroDelDominio() throws Exception {
        admin(ADMIN);
        Cookie sesion = loginAdmin(ADMIN);

        enviar(put("/perfil/email"), Map.of("emailNuevo", "nuevo@gmail.com", "passwordActual", PASSWORD), sesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.emailNuevo").exists());
        enviar(put("/perfil/email"), Map.of("emailNuevo", "nuevo@usal.edu.ar", "passwordActual", "Mala#1234"), sesion)
                .andExpect(status().isBadRequest());
        enviar(put("/perfil/email"), Map.of("emailNuevo", "nuevo@usal.edu.ar", "passwordActual", PASSWORD), sesion)
                .andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(jsonPath("$.email").value("nuevo@usal.edu.ar"));
    }

    @Test
    void elEmailDelAdminNoPuedeRepetirOtraCuenta() throws Exception {
        admin(ADMIN);
        usuario("ocupado@usal.edu.ar", "ACAD");

        enviar(
                        put("/perfil/email"),
                        Map.of("emailNuevo", "ocupado@usal.edu.ar", "passwordActual", PASSWORD),
                        loginAdmin(ADMIN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICADO"));
    }

    @Test
    void soloElAdminCambiaEmail() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        enviar(
                        put("/perfil/email"),
                        Map.of("emailNuevo", "nuevo@usal.edu.ar", "passwordActual", PASSWORD),
                        loginAspirante(EMAIL))
                .andExpect(status().isForbidden());
    }

    @Test
    void accesosYSesionesActivasMuestranLasDelTitular() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        loginAspirante(EMAIL);
        Cookie sesion = loginAspirante(EMAIL);

        mvc.perform(get("/perfil/accesos").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").exists())
                .andExpect(jsonPath("$[0].ip").exists())
                .andExpect(jsonPath("$[0].duracionMinutos").value(0));
        mvc.perform(get("/perfil/sesiones").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.actual == true)]", hasSize(1)));
    }

    @Test
    void elHistorialDeAccesosTieneDiezFilasComoMaximo() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = null;
        for (int i = 0; i < 11; i++) {
            sesion = loginAspirante(EMAIL);
        }

        mvc.perform(get("/perfil/accesos").cookie(sesion)).andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void cerrarOtraSesionPropiaLaCorta() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie vieja = loginAspirante(EMAIL);
        Cookie actual = loginAspirante(EMAIL);
        UUID idVieja = jdbc.queryForObject("SELECT id FROM sesiones ORDER BY inicio LIMIT 1", UUID.class);

        mvc.perform(delete("/perfil/sesiones/" + idVieja).cookie(actual)).andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").cookie(vieja)).andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/me").cookie(actual)).andExpect(status().isOk());
    }

    @Test
    void noSePuedeCerrarLaSesionDeOtraPersona() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        aspiranteValidado("otra@gmail.com", "40999999");
        Cookie ajena = loginAspirante("otra@gmail.com");
        UUID idAjena = jdbc.queryForObject("SELECT id FROM sesiones LIMIT 1", UUID.class);

        mvc.perform(delete("/perfil/sesiones/" + idAjena).cookie(loginAspirante(EMAIL)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/auth/me").cookie(ajena)).andExpect(status().isOk());
    }

    @Test
    void unIdDeSesionInvalidoNoEsUnErrorDelServidor() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        mvc.perform(delete("/perfil/sesiones/no-es-uuid").cookie(loginAspirante(EMAIL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinSesionNoHayPerfil() throws Exception {
        mvc.perform(get("/perfil")).andExpect(status().isUnauthorized());
    }
}
