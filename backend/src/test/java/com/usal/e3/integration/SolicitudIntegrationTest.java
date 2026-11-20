package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.PermisoSolicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.PermisoSolicitudRepository;
import jakarta.servlet.http.Cookie;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RF-ASP-001, RF-ASP-003, RF-AUTH-005 y los catálogos del contrato (§5 y §6). */
class SolicitudIntegrationTest extends IntegracionBase {

    private static final String EMAIL = "juan.perez@gmail.com";

    @Autowired
    private PermisoSolicitudRepository permisosSolicitud;

    @Test
    void elAspiranteCreaLaSolicitudEnSolIniYSinResponsable() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        postear("/solicitudes", datosSolicitud("ING-INF-001"), loginAspirante(EMAIL))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/solicitudes/\\d+")))
                .andExpect(jsonPath("$.codigoExp").value(matchesPattern("EXP-\\d{4}-\\d{3,}")))
                .andExpect(jsonPath("$.estado.codigo").value("SOL-INI"))
                .andExpect(jsonPath("$.aspirante.dni").value("40123456"))
                .andExpect(jsonPath("$.datosOrigen.universidad").value("Universidad Nacional de La Plata"))
                .andExpect(jsonPath("$.carreraDestino.codigo").value("ING-INF-001"))
                .andExpect(jsonPath("$.responsableAcademico").doesNotExist())
                .andExpect(jsonPath("$.miRelacion").value("ASPIRANTE"))
                .andExpect(jsonPath("$.accionesDisponibles[0]").value("ENVIAR_DOCUMENTACION"))
                .andExpect(jsonPath("$.chatHabilitado").value(false));

        assertThat(filasAuditoria(EMAIL, "cambio_estado")).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM estados_solicitud WHERE estado_codigo = 'SOL-INI' AND estado_anterior_codigo IS NULL",
                        Integer.class))
                .isEqualTo(1);
    }

    @Test
    void laCarreraDestinoTieneQueEstarActivaEnElCatalogo() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);

        postear("/solicitudes", datosSolicitud("NO-EXISTE"), sesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.carreraDestinoCodigo").exists());

        jdbc.update("UPDATE carreras SET activo = FALSE WHERE codigo = 'ING-CIV-001'");
        try {
            postear("/solicitudes", datosSolicitud("ING-CIV-001"), sesion)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.carreraDestinoCodigo").exists());
        } finally {
            jdbc.update("UPDATE carreras SET activo = TRUE WHERE codigo = 'ING-CIV-001'");
        }
    }

    @Test
    void elFormularioInicialTieneCamposObligatorios() throws Exception {
        aspiranteValidado(EMAIL, "40123456");

        postear("/solicitudes", Map.of("carreraDestinoCodigo", "ING-INF-001"), loginAspirante(EMAIL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.universidadOrigen").exists())
                .andExpect(jsonPath("$.errors.condicionAcademica").exists());
    }

    @Test
    void elPersonalNoCreaSolicitudes() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");

        postear("/solicitudes", datosSolicitud("ING-INF-001"), loginPersonal("m.gomez@usal.edu.ar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
    }

    @Test
    void unAspiranteNoVeLasSolicitudesDeOtro() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        int id = crearSolicitud(loginAspirante(EMAIL));
        aspiranteValidado("otro@gmail.com", "30123456");
        Cookie otro = loginAspirante("otro@gmail.com");

        mvc.perform(get("/solicitudes/" + id).cookie(otro)).andExpect(status().isNotFound());
        mvc.perform(get("/solicitudes/" + id + "/historial").cookie(otro)).andExpect(status().isNotFound());
        mvc.perform(get("/solicitudes/" + id + "/documentos").cookie(otro)).andExpect(status().isNotFound());
        mvc.perform(get("/solicitudes").cookie(otro)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void elListadoDelAspirantePoneArribaLoQueEsperaSuAccion() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);
        int enviada = crearSolicitud(sesion);
        enviarDocumentacion(sesion, enviada);
        int nueva = crearSolicitud(sesion);

        mvc.perform(get("/solicitudes").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(nueva))
                .andExpect(jsonPath("$.content[0].requiereMiAccion").value(true))
                .andExpect(jsonPath("$.content[0].fechaCreacion").isNotEmpty())
                .andExpect(jsonPath("$.content[1].id").value(enviada))
                .andExpect(jsonPath("$.content[1].estado.codigo").value("DOC-REV"))
                .andExpect(jsonPath("$.content[1].requiereMiAccion").value(false));
        mvc.perform(get("/solicitudes?estado=DOC-REV").cookie(sesion))
                .andExpect(jsonPath("$.content", hasSize(1)));
        mvc.perform(get("/solicitudes?finalizadas=true").cookie(sesion))
                .andExpect(jsonPath("$.content", empty()));
        mvc.perform(get("/solicitudes?size=1&page=1").cookie(sesion))
                .andExpect(jsonPath("$.content[0].id").value(enviada))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void laBandejaDelPersonalTodaviaNoEstaHabilitada() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");

        mvc.perform(get("/solicitudes").cookie(loginPersonal("m.gomez@usal.edu.ar")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elPersonalVeTodasYSoloParticipaConPermisoActivo() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        int id = crearSolicitud(loginAspirante(EMAIL));
        Usuario admin = admin("admin@usal.edu.ar");

        usuario("observador@usal.edu.ar", "ACAD");
        assertRelacion(id, loginPersonal("observador@usal.edu.ar"), "OBSERVADOR");

        permisoCarrera(usuario("carrera@usal.edu.ar", "SECRE"), "ING-INF-001");
        assertRelacion(id, loginPersonal("carrera@usal.edu.ar"), "PARTICIPANTE");

        PermisoCarrera revocado = permisoCarrera(usuario("revocado@usal.edu.ar", "ACAD"), "ING-INF-001");
        revocado.setRevokedAt(OffsetDateTime.now());
        guardar(revocado);
        assertRelacion(id, loginPersonal("revocado@usal.edu.ar"), "OBSERVADOR");

        Usuario temporal = usuario("temporal@usal.edu.ar", "ACAD");
        permisosSolicitud.save(new PermisoSolicitud(temporal.getId(), id, admin.getId(), null));
        assertRelacion(id, loginPersonal("temporal@usal.edu.ar"), "PARTICIPANTE");

        Usuario vencido = usuario("vencido@usal.edu.ar", "ACAD");
        permisosSolicitud.save(
                new PermisoSolicitud(vencido.getId(), id, admin.getId(), OffsetDateTime.now().minusHours(1)));
        assertRelacion(id, loginPersonal("vencido@usal.edu.ar"), "OBSERVADOR");

        usuario("revisor@usal.edu.ar", "REVISOR");
        assertRelacion(id, loginPersonal("revisor@usal.edu.ar"), "REVISOR");
        assertRelacion(id, loginAdmin("admin@usal.edu.ar"), "ADMIN");
    }

    @Test
    void losCatalogosVienenDelBackend() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        Cookie sesion = loginAspirante(EMAIL);

        mvc.perform(get("/carreras").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].codigo").value("ING-CIV-001"))
                .andExpect(jsonPath("$[0].facultad").value("Facultad de Ingeniería"))
                .andExpect(jsonPath("$[0].activo").value(true));
        mvc.perform(get("/estados").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(21)))
                .andExpect(jsonPath("$[0].codigo").value("SOL-INI"))
                .andExpect(jsonPath("$[12].codigo").value("INSC-RECH"))
                .andExpect(jsonPath("$[12].esTerminal").value(true))
                .andExpect(jsonPath("$[20].codigo").value("SOL-CANC"))
                .andExpect(jsonPath("$[20].esTerminal").value(true));
        mvc.perform(get("/carreras")).andExpect(status().isUnauthorized());
    }

    @Test
    void soloElAdminVeLasCarrerasInactivas() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        admin("admin@usal.edu.ar");
        jdbc.update("UPDATE carreras SET activo = FALSE WHERE codigo = 'ING-CIV-001'");
        try {
            mvc.perform(get("/carreras?activo=todas").cookie(loginAspirante(EMAIL)))
                    .andExpect(jsonPath("$", hasSize(2)));
            mvc.perform(get("/carreras?activo=todas").cookie(loginAdmin("admin@usal.edu.ar")))
                    .andExpect(jsonPath("$", hasSize(3)));
        } finally {
            jdbc.update("UPDATE carreras SET activo = TRUE WHERE codigo = 'ING-CIV-001'");
        }
    }

    private void assertRelacion(int id, Cookie sesion, String relacion) throws Exception {
        mvc.perform(get("/solicitudes/" + id).cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miRelacion").value(relacion))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
    }
}
