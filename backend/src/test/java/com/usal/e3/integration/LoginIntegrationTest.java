package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.security.CookieSesion;
import com.usal.e3.service.AuthService;
import com.usal.e3.service.ClienteHttp;
import jakarta.servlet.http.Cookie;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

/** RF-AUTH-002 a 005: logins, bloqueo por intentos, sesión y RBAC. */
class LoginIntegrationTest extends IntegracionBase {

    private static final String EMAIL = "juan.perez@gmail.com";
    private static final ClienteHttp CLIENTE = new ClienteHttp("10.0.0.1", "test");

    @Autowired
    private AuthService auth;

    private Map<String, String> malLogin(String email) {
        return Map.of("email", email, "password", "Incorrecta#1");
    }

    @Test
    void tresFallosBloqueanSeisHorasAunqueLaContrasenaSeaCorrecta() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        postear("/auth/login", malLogin(EMAIL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.intentosRestantes").value(2));
        postear("/auth/login", malLogin(EMAIL)).andExpect(jsonPath("$.intentosRestantes").value(1));
        postear("/auth/login", malLogin(EMAIL))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.bloqueadoHasta").exists());

        postear("/auth/login", Map.of("email", EMAIL, "password", PASSWORD)).andExpect(status().isLocked());
        assertThat(aspirantes.findByEmail(EMAIL).orElseThrow().getIntentosLoginFallidos()).isEqualTo(3);
        assertThat(filasAuditoria(EMAIL, "bloqueo_cuenta")).isEqualTo(1);
    }

    @Test
    void unLoginCorrectoReiniciaElContador() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        postear("/auth/login", malLogin(EMAIL));
        postear("/auth/login", malLogin(EMAIL));

        loginAspirante(EMAIL);

        postear("/auth/login", malLogin(EMAIL)).andExpect(jsonPath("$.intentosRestantes").value(2));
    }

    @Test
    void cuandoVenceElBloqueoPuedeVolverAEntrar() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        for (int i = 0; i < 3; i++) {
            postear("/auth/login", malLogin(EMAIL));
        }
        jdbc.update("UPDATE aspirantes SET bloqueado_hasta = NOW() - INTERVAL '1 minute'");

        loginAspirante(EMAIL);
    }

    @Test
    void elAdminDesbloqueaUnaCuentaYElContadorVuelveACero() throws Exception {
        Aspirante aspirante = aspiranteValidado(EMAIL, "40123456");
        for (int i = 0; i < 3; i++) {
            postear("/auth/login", malLogin(EMAIL));
        }
        admin("admin@usal.edu.ar");
        Cookie sesionAdmin = loginAdmin("admin@usal.edu.ar");

        postear("/admin/cuentas/aspirante/" + aspirante.getId() + "/desbloquear", Map.of(), sesionAdmin)
                .andExpect(status().isNoContent());

        assertThat(aspirantes.findByEmail(EMAIL).orElseThrow().getIntentosLoginFallidos()).isZero();
        assertThat(filasAuditoria("admin@usal.edu.ar", "desbloqueo_cuenta")).isEqualTo(1);
        loginAspirante(EMAIL);
    }

    @Test
    void credencialesDeUnEmailInexistenteDanElMismoError() throws Exception {
        postear("/auth/login", malLogin("nadie@gmail.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void recordarmeDuraTreintaDias() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        Cookie normal = loginAspirante(EMAIL);
        Cookie recordada = cookie(postear(
                        "/auth/login", Map.of("email", EMAIL, "password", PASSWORD, "recordarme", true))
                .andExpect(status().isOk()));

        assertThat(normal.getMaxAge()).isEqualTo(86_400);
        assertThat(recordada.getMaxAge()).isEqualTo(2_592_000);
    }

    @Test
    void logoutCortaLaSesionAlInstante() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);

        postear("/auth/logout", Map.of(), sesion).andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(status().isUnauthorized());
    }

    @Test
    void sinSesionResponde401ConElFormatoDelContrato() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"))
                .andExpect(jsonPath("$.path").value("/auth/me"));
    }

    @Test
    void unaCookieManipuladaNoAutentica() throws Exception {
        mvc.perform(get("/auth/me").cookie(new Cookie(CookieSesion.NOMBRE, "abc.def.ghi")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaSesionVencidaNoAutentica() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);
        jdbc.update("UPDATE sesiones SET expira_en = NOW() - INTERVAL '1 minute'");

        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(status().isUnauthorized());
    }

    @Test
    void desactivarUnUsuarioLeCortaElAccesoAunqueTengaSesionAbierta() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        Cookie sesion = loginDesarrollo("m.gomez@usal.edu.ar");
        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(status().isOk());

        jdbc.update("UPDATE usuarios SET activo = FALSE");

        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(status().isUnauthorized());
    }

    @Test
    void elAdminEntraConUsuarioYContrasena() throws Exception {
        admin("admin@usal.edu.ar");

        postear("/auth/admin/login", Map.of("usuario", "admin@usal.edu.ar", "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.esOAuth").value(false));
        assertThat(filasAuditoria("admin@usal.edu.ar", "login")).isEqualTo(1);
    }

    @Test
    void elPersonalNoEntraPorElLoginDeAdmin() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");

        postear("/auth/admin/login", Map.of("usuario", "m.gomez@usal.edu.ar", "password", PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lasRutasDeAdminSonSoloParaElAdmin() throws Exception {
        Aspirante aspirante = aspiranteValidado(EMAIL, "40123456");
        usuario("m.gomez@usal.edu.ar", "ACAD");
        String ruta = "/admin/cuentas/aspirante/" + aspirante.getId() + "/desbloquear";

        postear(ruta, Map.of()).andExpect(status().isUnauthorized());
        postear(ruta, Map.of(), loginAspirante(EMAIL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        postear(ruta, Map.of(), loginDesarrollo("m.gomez@usal.edu.ar")).andExpect(status().isForbidden());
    }

    @Test
    void lasCarrerasDeLaSesionExcluyenLosPermisosRevocados() throws Exception {
        Usuario academico = usuario("m.gomez@usal.edu.ar", "ACAD");
        permisoCarrera(academico, "ING-INF-001");
        PermisoCarrera revocado = permisoCarrera(academico, "ING-CIV-001");
        Cookie sesion = loginDesarrollo("m.gomez@usal.edu.ar");
        mvc.perform(get("/auth/me").cookie(sesion)).andExpect(jsonPath("$.carrerasConPermiso.length()").value(2));

        revocado.setRevokedAt(OffsetDateTime.now());
        guardar(revocado);

        mvc.perform(get("/auth/me").cookie(sesion))
                .andExpect(jsonPath("$.carrerasConPermiso.length()").value(1))
                .andExpect(jsonPath("$.carrerasConPermiso[0].codigo").value("ING-INF-001"));
    }

    @Test
    void cadaRolDelPersonalEntraYVeSuRol() throws Exception {
        for (String rol : new String[] {"ACAD", "SECRE", "REVISOR"}) {
            String email = rol.toLowerCase() + "@usal.edu.ar";
            usuario(email, rol);

            mvc.perform(get("/auth/me").cookie(loginDesarrollo(email)))
                    .andExpect(jsonPath("$.tipo").value("USUARIO"))
                    .andExpect(jsonPath("$.rol").value(rol))
                    .andExpect(jsonPath("$.esOAuth").value(true));
        }
    }

    @Test
    void googleAceptaAlPersonalRegistradoYGuardaElSub() {
        usuario("m.gomez@usal.edu.ar", "ACAD");

        auth.loginGoogle("M.Gomez@usal.edu.ar", "sub-1", true, CLIENTE);

        assertThat(usuarios.findByEmail("m.gomez@usal.edu.ar").orElseThrow().getGoogleSub()).isEqualTo("sub-1");
        assertThat(filasAuditoria("m.gomez@usal.edu.ar", "login")).isEqualTo(1);
    }

    @Test
    void googleRechazaLoQueNoEsPersonalActivoDeUsal() {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        Usuario inactivo = usuario("baja@usal.edu.ar", "ACAD");
        inactivo.setActivo(false);
        usuarios.save(inactivo);
        admin("admin@usal.edu.ar");

        rechazaGoogle("alguien@gmail.com", "sub", true);
        rechazaGoogle("noregistrado@usal.edu.ar", "sub", true);
        rechazaGoogle("baja@usal.edu.ar", "sub", true);
        rechazaGoogle("admin@usal.edu.ar", "sub", true);
        rechazaGoogle("m.gomez@usal.edu.ar", "sub", false);
    }

    @Test
    void googleRechazaOtraCuentaConElMismoEmail() {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        auth.loginGoogle("m.gomez@usal.edu.ar", "sub-1", true, CLIENTE);

        rechazaGoogle("m.gomez@usal.edu.ar", "sub-2", true);
    }

    private void rechazaGoogle(String email, String sub, boolean verificado) {
        assertThatThrownBy(() -> auth.loginGoogle(email, sub, verificado, CLIENTE))
                .isInstanceOfSatisfying(
                        ApiException.class,
                        ex -> assertThat(ex.code()).isEqualTo(ErrorCode.USUARIO_NO_REGISTRADO));
    }

    @Test
    void elIngresoConGoogleTerminaEnLaPantallaDeGoogle() throws Exception {
        mvc.perform(get("/auth/google")).andExpect(redirectedUrl("/oauth2/authorization/google"));
        mvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/**"));
    }

    @Test
    void elCorsAceptaLosOrigenesDelFrontendConCredenciales() throws Exception {
        for (String origen : new String[] {"http://localhost:5173", "http://localhost:5273"}) {
            mvc.perform(options("/auth/login")
                            .header(HttpHeaders.ORIGIN, origen)
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origen))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        }
        mvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://sitio-malo.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
