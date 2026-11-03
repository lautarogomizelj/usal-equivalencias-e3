package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

/** RF-AUTH-001 y el recupero de contraseña de RF-AUTH-002. */
class RegistroIntegrationTest extends IntegracionBase {

    private static final String EMAIL = "juan.perez@gmail.com";

    @Test
    void registroValidaElCodigoEIniciaSesion() throws Exception {
        String codigo = registrar(EMAIL, "40123456");

        ResultActions respuesta = postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ASPIRANTE"))
                .andExpect(jsonPath("$.rol").value("ASPIRANTE"))
                .andExpect(jsonPath("$.emailValidado").value(true))
                .andExpect(jsonPath("$.esOAuth").value(false))
                .andExpect(jsonPath("$.carrerasConPermiso").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));

        mvc.perform(get("/auth/me").cookie(cookie(respuesta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
        assertThat(filasAuditoria(EMAIL, "crear_cuenta")).isEqualTo(1);
        assertThat(filasAuditoria(EMAIL, "validar_email")).isEqualTo(1);
        assertThat(filasAuditoria(EMAIL, "login")).isEqualTo(1);
    }

    @Test
    void elEmailSeGuardaEnMinusculaYElCodigoNoQuedaEnClaro() throws Exception {
        postear("/auth/registro", datosRegistro("Juan.Perez@GMAIL.com", "40123456")).andExpect(status().isCreated());

        String codigo = ultimoCodigoValidacion(EMAIL);
        String hash = jdbc.queryForObject("SELECT codigo_hash FROM codigos_verificacion", String.class);
        assertThat(hash).isNotEqualTo(codigo).startsWith("$2");
        assertThat(aspirantes.existsByEmail(EMAIL)).isTrue();
    }

    @Test
    void emailODniRepetidosDevuelvenElCampoEnConflicto() throws Exception {
        registrar(EMAIL, "40123456");

        postear("/auth/registro", datosRegistro(EMAIL, "40999999"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICADO"))
                .andExpect(jsonPath("$.errors.email").exists());
        postear("/auth/registro", datosRegistro("otro@gmail.com", "40123456"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.dni").exists());
    }

    @Test
    void validaLosCamposComoElFrontend() throws Exception {
        Map<String, Object> datos = new HashMap<>(datosRegistro("no-es-email", "123"));
        datos.put("password", "sinmayuscula1!");
        datos.put("telefono", "abc");

        postear("/auth/registro", datos)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.dni").exists())
                .andExpect(jsonPath("$.errors.telefono").exists());
    }

    @Test
    void sinValidarNoPuedeEntrar() throws Exception {
        registrar(EMAIL, "40123456");

        postear("/auth/login", Map.of("email", EMAIL, "password", PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NO_VALIDADO"));
    }

    @Test
    void tresCodigosMalosBloqueanQuinceMinutos() throws Exception {
        String codigo = registrar(EMAIL, "40123456");
        String malo = codigo.equals("000000") ? "000001" : "000000";
        Map<String, String> pedido = Map.of("email", EMAIL, "codigo", malo);

        postear("/auth/registro/validar", pedido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODIGO_INVALIDO"))
                .andExpect(jsonPath("$.intentosRestantes").value(2));
        postear("/auth/registro/validar", pedido).andExpect(jsonPath("$.intentosRestantes").value(1));
        postear("/auth/registro/validar", pedido)
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.bloqueadoHasta").exists());

        postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", codigo))
                .andExpect(status().isLocked());
    }

    @Test
    void reenviarInvalidaElCodigoAnteriorYNoDaIntentosNuevos() throws Exception {
        String viejo = registrar(EMAIL, "40123456");
        String malo = viejo.equals("000000") ? "000001" : "000000";
        postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", malo))
                .andExpect(jsonPath("$.intentosRestantes").value(2));

        postear("/auth/registro/reenviar-codigo", Map.of("email", EMAIL)).andExpect(status().isNoContent());
        String nuevo = ultimoCodigoValidacion(EMAIL);
        String invalido = viejo.equals(nuevo) ? malo : viejo;

        postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", invalido))
                .andExpect(jsonPath("$.intentosRestantes").value(1));
        postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", nuevo)).andExpect(status().isOk());
    }

    @Test
    void unCodigoVencidoNoSirve() throws Exception {
        String codigo = registrar(EMAIL, "40123456");
        jdbc.update("UPDATE codigos_verificacion SET expira_en = NOW() - INTERVAL '1 minute'");

        postear("/auth/registro/validar", Map.of("email", EMAIL, "codigo", codigo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODIGO_EXPIRADO"));
    }

    @Test
    void reenviarNoRevelaSiElEmailExiste() throws Exception {
        postear("/auth/registro/reenviar-codigo", Map.of("email", "nadie@gmail.com"))
                .andExpect(status().isNoContent());

        verify(mail, never()).enviarCodigoValidacion(anyString(), anyString());
    }

    @Test
    void recuperarNoRevelaSiElEmailExiste() throws Exception {
        postear("/auth/password/recuperar", Map.of("email", "nadie@gmail.com")).andExpect(status().isNoContent());

        verify(mail, never()).enviarCodigoRecupero(anyString(), anyString());
    }

    @Test
    void restablecerCambiaLaContrasenaYCierraLasSesionesAbiertas() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesionVieja = loginAspirante(EMAIL);

        postear("/auth/password/recuperar", Map.of("email", EMAIL)).andExpect(status().isNoContent());
        Map<String, String> pedido =
                Map.of("email", EMAIL, "codigo", ultimoCodigoRecupero(EMAIL), "passwordNueva", "Nueva#2026");
        postear("/auth/password/restablecer", pedido).andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").cookie(sesionVieja)).andExpect(status().isUnauthorized());
        postear("/auth/login", Map.of("email", EMAIL, "password", PASSWORD)).andExpect(status().isUnauthorized());
        postear("/auth/login", Map.of("email", EMAIL, "password", "Nueva#2026")).andExpect(status().isOk());
        postear("/auth/password/restablecer", pedido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODIGO_INVALIDO"));
    }

    @Test
    void unaContrasenaDebilNoSeAceptaAlRestablecer() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        postear("/auth/password/recuperar", Map.of("email", EMAIL));

        postear("/auth/password/restablecer", Map.of(
                        "email", EMAIL, "codigo", ultimoCodigoRecupero(EMAIL), "passwordNueva", "debil"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.passwordNueva").exists());
    }
}
