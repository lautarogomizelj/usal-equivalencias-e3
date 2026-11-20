package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/** RF-PROC-001, 003 y 004: bandeja del personal, tomar proceso y darse de baja. */
class ProcesoIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final String OTRO_ACADEMICO = "p.sosa@usal.edu.ar";

    private Cookie aspirante;
    private Cookie academico;

    @BeforeEach
    void preparar() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
    }

    @Test
    void laBandejaPonePrimeroLasCarrerasConPermisoYDespuesElRestoAlfabetico() throws Exception {
        int industrial = crearSolicitud(aspirante, "ING-IND-001");
        int informatica = crearSolicitud(aspirante, "ING-INF-001");
        int civil = crearSolicitud(aspirante, "ING-CIV-001");

        mvc.perform(get("/solicitudes").cookie(academico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].id", contains(informatica, civil, industrial)))
                .andExpect(jsonPath("$.content[*].esDeMisCarreras", contains(true, false, false)))
                .andExpect(jsonPath("$.content[0].aspirante.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.content[0].requiereMiAccion").value(false));

        usuario("observador@usal.edu.ar", "ACAD");
        mvc.perform(get("/solicitudes").cookie(loginPersonal("observador@usal.edu.ar")))
                .andExpect(jsonPath("$.content[*].id", contains(civil, informatica, industrial)))
                .andExpect(jsonPath("$.content[*].esDeMisCarreras", contains(false, false, false)));
    }

    @Test
    void laBandejaFiltraPorEstadoCarreraFechaTextoYAsignadas() throws Exception {
        int enviada = crearSolicitud(aspirante, "ING-INF-001");
        enviarDocumentacion(aspirante, enviada);
        int civil = crearSolicitud(aspirante, "ING-CIV-001");
        int cancelada = crearSolicitud(aspirante, "ING-IND-001");
        postear("/solicitudes/" + cancelada + "/cancelar", Map.of("estadoEsperado", "SOL-INI"), aspirante)
                .andExpect(status().isOk());

        bandeja("estado=DOC-REV").andExpect(jsonPath("$.content[*].id", contains(enviada)));
        bandeja("estado=DOC-REV&estado=SOL-CANC").andExpect(jsonPath("$.content", hasSize(2)));
        bandeja("finalizadas=true").andExpect(jsonPath("$.content[*].id", contains(cancelada)));
        bandeja("finalizadas=false").andExpect(jsonPath("$.content", hasSize(2)));
        bandeja("q=civil").andExpect(jsonPath("$.content[*].id", contains(civil)));
        bandeja("q=PÉREZ").andExpect(jsonPath("$.content", hasSize(3)));
        bandeja("q=EXP-").andExpect(jsonPath("$.content", hasSize(3)));
        bandeja("q=nadie").andExpect(jsonPath("$.content", empty()));
        // Los filtros por día son días de Argentina, no los de la zona donde corre el test.
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        bandeja("desde=" + hoy.plusDays(1)).andExpect(jsonPath("$.content", empty()));
        bandeja("desde=" + hoy + "&hasta=" + hoy).andExpect(jsonPath("$.content", hasSize(3)));
        bandeja("hasta=" + hoy.minusDays(1)).andExpect(jsonPath("$.content", empty()));
        bandeja("carreraId=" + jdbc.queryForObject("SELECT id FROM carreras WHERE codigo = 'ING-CIV-001'", Integer.class))
                .andExpect(jsonPath("$.content[*].id", contains(civil)));

        // Sin responsable: las abiertas que puede tomar, o sea las de su carrera.
        bandeja("sinResponsable=true").andExpect(jsonPath("$.content[*].id", contains(enviada)));
        bandeja("asignadas=true").andExpect(jsonPath("$.content", empty()));
        tomar(enviada, academico).andExpect(status().isOk());
        bandeja("sinResponsable=true").andExpect(jsonPath("$.content", empty()));
        bandeja("asignadas=true")
                .andExpect(jsonPath("$.content[*].id", contains(enviada)))
                .andExpect(jsonPath("$.content[0].requiereMiAccion").value(true))
                .andExpect(jsonPath("$.content[0].responsableAcademico.email").value(ACADEMICO));
    }

    @Test
    void tomarElProcesoAsignaAlResponsableSinCambiarElEstado() throws Exception {
        int id = crearSolicitud(aspirante);
        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");

        mvc.perform(get("/solicitudes/" + id).cookie(academico))
                .andExpect(jsonPath("$.miRelacion").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.accionesDisponibles", contains("TOMAR_PROCESO")));

        tomar(id, academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("SOL-INI"))
                .andExpect(jsonPath("$.responsableAcademico.email").value(ACADEMICO))
                .andExpect(jsonPath("$.responsableAcademico.fechaAsignacion").isNotEmpty())
                .andExpect(jsonPath("$.responsableAdministrativo").doesNotExist())
                .andExpect(jsonPath("$.accionesDisponibles", contains("DARSE_DE_BAJA")))
                .andExpect(jsonPath("$.chatHabilitado").value(true));

        assertThat(filasAuditoria(ACADEMICO, "tomar_proceso")).isEqualTo(1);
        verify(mail).enviarAviso(eq(OTRO_ACADEMICO), conTexto("ha tomado este proceso"), conTexto("María Gómez"));
        verify(mail, never()).enviarAviso(eq(ACADEMICO), conTexto("ha tomado este proceso"), anyString());
        mvc.perform(get("/solicitudes/" + id + "/historial").cookie(aspirante))
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_RESPONSABLE"))
                .andExpect(jsonPath("$[0].usuario").value("María Gómez"))
                .andExpect(jsonPath("$[0].comentario").value("Tomó el proceso como responsable académico."));
    }

    @Test
    void elProcesoYaTomadoNoSeVuelveATomar() throws Exception {
        int id = crearSolicitud(aspirante);
        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");
        Cookie otro = loginPersonal(OTRO_ACADEMICO);
        tomar(id, academico).andExpect(status().isOk());

        tomar(id, otro).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PROCESO_YA_TOMADO"));
        mvc.perform(get("/solicitudes/" + id).cookie(otro))
                .andExpect(jsonPath("$.miRelacion").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
    }

    @Test
    void deDosQueLoTomanALaVezSoloUnoQuedaAsignado() throws Exception {
        int id = crearSolicitud(aspirante);
        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");
        Cookie otro = loginPersonal(OTRO_ACADEMICO);

        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> intentos = List.of(
                    () -> tomar(id, academico).andReturn().getResponse().getStatus(),
                    () -> tomar(id, otro).andReturn().getResponse().getStatus());
            List<Integer> respuestas = new java.util.ArrayList<>();
            for (Future<Integer> intento : hilos.invokeAll(intentos)) {
                respuestas.add(intento.get());
            }
            assertThat(respuestas).containsExactlyInAnyOrder(200, 409);
        } finally {
            hilos.shutdown();
        }
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM auditoria_sistema WHERE accion = 'tomar_proceso'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void soloTomaElProcesoQuienTienePermisoYCadaRolElSuyo() throws Exception {
        int id = crearSolicitud(aspirante);
        usuario("observador@usal.edu.ar", "ACAD");
        permisoCarrera(usuario("c.ruiz@usal.edu.ar", "SECRE"), "ING-INF-001");
        usuario("revisor@usal.edu.ar", "REVISOR");

        tomar(id, loginPersonal("observador@usal.edu.ar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_PARTICIPANTE"));
        tomar(id, loginPersonal("revisor@usal.edu.ar")).andExpect(status().isForbidden());
        tomar(id, aspirante).andExpect(status().isForbidden());
        tomar(999, academico).andExpect(status().isNotFound());

        tomar(id, academico).andExpect(status().isOk());
        tomar(id, loginPersonal("c.ruiz@usal.edu.ar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsableAcademico.email").value(ACADEMICO))
                .andExpect(jsonPath("$.responsableAdministrativo.email").value("c.ruiz@usal.edu.ar"));
    }

    @Test
    void unaSolicitudCerradaNoSeToma() throws Exception {
        int id = crearSolicitud(aspirante);
        postear("/solicitudes/" + id + "/cancelar", Map.of("estadoEsperado", "SOL-INI"), aspirante)
                .andExpect(status().isOk());

        mvc.perform(get("/solicitudes/" + id).cookie(academico))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
        tomar(id, academico).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void elUnicoConAccesoNoPuedeDarseDeBaja() throws Exception {
        int id = crearSolicitud(aspirante);
        tomar(id, academico).andExpect(jsonPath("$.accionesDisponibles", not(hasItem("DARSE_DE_BAJA"))));

        baja(id, academico).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("UNICO_RESPONSABLE"));
        mvc.perform(get("/solicitudes/" + id).cookie(academico))
                .andExpect(jsonPath("$.responsableAcademico.email").value(ACADEMICO));
    }

    @Test
    void alDarseDeBajaElProcesoVuelveAEstarDisponible() throws Exception {
        int id = crearSolicitud(aspirante);
        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");
        Cookie otro = loginPersonal(OTRO_ACADEMICO);
        tomar(id, academico).andExpect(status().isOk());

        baja(id, otro).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        baja(id, academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsableAcademico").doesNotExist())
                .andExpect(jsonPath("$.accionesDisponibles", contains("TOMAR_PROCESO")))
                .andExpect(jsonPath("$.chatHabilitado").value(false));

        assertThat(filasAuditoria(ACADEMICO, "baja_responsable")).isEqualTo(1);
        verify(mail).enviarAviso(eq(OTRO_ACADEMICO), conTexto("vuelve a estar disponible"), anyString());
        mvc.perform(get("/solicitudes/" + id + "/historial").cookie(otro))
                .andExpect(jsonPath("$[0].comentario").value("Se dio de baja como responsable académico."));
        tomar(id, otro).andExpect(status().isOk()).andExpect(jsonPath("$.responsableAcademico.email").value(OTRO_ACADEMICO));
    }

    @Test
    void laSolicitudNuevaAvisaAQuienesTienenAccesoALaCarrera() throws Exception {
        permisoCarrera(usuario("c.ruiz@usal.edu.ar", "SECRE"), "ING-INF-001");
        usuario("observador@usal.edu.ar", "ACAD");

        crearSolicitud(aspirante);

        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("nueva solicitud"), conTexto("Juan Pérez"));
        verify(mail).enviarAviso(eq("c.ruiz@usal.edu.ar"), conTexto("nueva solicitud"), anyString());
        verify(mail, never()).enviarAviso(eq("observador@usal.edu.ar"), anyString(), anyString());
    }

    private ResultActions bandeja(String filtros) throws Exception {
        return mvc.perform(get("/solicitudes?" + filtros).cookie(academico)).andExpect(status().isOk());
    }

    private ResultActions tomar(int id, Cookie sesion) throws Exception {
        return mvc.perform(post("/solicitudes/" + id + "/responsables/tomar").cookie(sesion));
    }

    private ResultActions baja(int id, Cookie sesion) throws Exception {
        return mvc.perform(post("/solicitudes/" + id + "/responsables/baja").cookie(sesion));
    }
}
