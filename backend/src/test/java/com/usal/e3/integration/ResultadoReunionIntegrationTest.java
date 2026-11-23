package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

/**
 * RF-ACAD-003, RF-ACAD-004 y RF-ACAD-006: la fecha pautada por chat (transición 11), el resultado de la reunión
 * (12 y 13), la actualización del análisis en `ANAL-POST` y la decisión de inscripción (14 y 15).
 */
class ResultadoReunionIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private Cookie aspirante;
    private Cookie academico;
    private int solicitud;

    @BeforeEach
    void dejarLaSolicitudConLaPrimeraRondaPropuesta() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
        mvc.perform(post(ruta("/responsables/tomar")).cookie(academico)).andExpect(status().isOk());
        postear(ruta("/documentacion/aceptar"), Map.of("estadoEsperado", "DOC-REV"), academico)
                .andExpect(status().isOk());
        Map<String, Object> datos =
                Map.of("estadoEsperado", "ANAL-PEND", "horarios", new Object[] {Map.of("inicio", horario(2, 10))});
        mvc.perform(multipart(ruta("/analisis/propuesta"))
                        .file(new MockMultipartFile("datos", "", "application/json", json.writeValueAsBytes(datos)))
                        .param("matrizNotas", "Se reconocen 6 materias")
                        .param("horarioNotas", "Turno noche")
                        .cookie(academico))
                .andExpect(status().isOk());
    }

    @Test
    void elAcademicoFijaLaFechaPautadaPorChat() throws Exception {
        postear(ruta("/reunion/solicitar-otras-fechas"),
                        Map.of("estadoEsperado", "REU-FEC-ASP-1", "motivo", "Trabajo", "disponibilidad", "De noche"),
                        aspirante)
                .andExpect(status().isOk());
        segundaRonda(horario(4, 16));
        postear(ruta("/reunion/rechazar-ronda"), Map.of("estadoEsperado", "REU-FEC-ASP-2"), aspirante)
                .andExpect(jsonPath("$.estado.codigo").value("REU-CHAT"));
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("FIJAR_FECHA")));

        // La fecha se pautó por chat: no tiene que caer en las franjas de las rondas, solo ser a futuro.
        String pautada = LocalDate.now(ZONA).plusDays(3).atTime(20, 30).atZone(ZONA).toOffsetDateTime().toString();
        postear(ruta("/reunion/fijar-fecha"), Map.of("estadoEsperado", "REU-CHAT"), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.inicio").exists());
        postear(ruta("/reunion/fijar-fecha"),
                        Map.of("estadoEsperado", "REU-CHAT", "inicio", OffsetDateTime.now().minusMinutes(5).toString()),
                        academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.inicio").exists());
        postear(ruta("/reunion/fijar-fecha"), Map.of("estadoEsperado", "REU-CHAT", "inicio", pautada), aspirante)
                .andExpect(status().isForbidden());
        postear(ruta("/reunion/fijar-fecha"), Map.of("estadoEsperado", "REU-PEND", "inicio", pautada), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));

        postear(ruta("/reunion/fijar-fecha"), Map.of("estadoEsperado", "REU-CHAT", "inicio", pautada), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-PEND"))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("CARGAR_ENLACE_MEET")));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.reunion.origen").value("FIJADA_CHAT"))
                .andExpect(jsonPath("$.reunion.resultado").value("PENDIENTE"));
        assertThat(jdbc.queryForObject(
                        "SELECT fin = inicio + INTERVAL '1 hour' AND horario_propuesto_id IS NULL FROM reuniones",
                        Boolean.class))
                .isTrue();
    }

    @Test
    void siElAspiranteNoSePresentaSeVuelveAProponerFechas() throws Exception {
        pactarReunion(1);

        postear(ruta("/reunion/no-asistio"), Map.of("estadoEsperado", "REU-PEND"), aspirante)
                .andExpect(status().isForbidden());
        postear(ruta("/reunion/no-asistio"), Map.of("estadoEsperado", "REU-CHAT"), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        postear(ruta("/reunion/no-asistio"),
                        Map.of("estadoEsperado", "REU-PEND", "comentario", "Esperé 20 minutos"),
                        academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ACAD"))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("PROPONER_SEGUNDA_RONDA")));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("no registramos tu asistencia"), conTexto("nuevas fechas"));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.reunion.resultado").value("NO_ASISTIO"));
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.ultimaObservacion.comentario").value("Esperé 20 minutos"));

        // Se reprograma por el flujo normal: el Académico propone y el aspirante elige. Es una reunión nueva.
        segundaRonda(horario(4, 16));
        pactarReunion(2);
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.reunion.origen").value("ELEGIDA_ASPIRANTE"))
                .andExpect(jsonPath("$.reunion.resultado").value("PENDIENTE"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM reuniones", Integer.class)).isEqualTo(2);

        // Si vuelve a faltar con la 2ª ronda ya usada, se propone otra 2ª ronda (interfaces, Anexo A #7).
        postear(ruta("/reunion/no-asistio"), Map.of("estadoEsperado", "REU-PEND"), academico)
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ACAD"));
        segundaRonda(horario(6, 11));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.rondaActual").value(2))
                .andExpect(jsonPath("$.rondas[2].horarios[0].id").value(3));
    }

    @Test
    void laReunionFinalizaYLasNotasSonSoloDelPersonal() throws Exception {
        pactarReunion(1);
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath(
                        "$.accionesDisponibles",
                        containsInAnyOrder("FINALIZAR_REUNION", "MARCAR_NO_ASISTIO", "CARGAR_ENLACE_MEET")));

        postear(ruta("/reunion/finalizar"), Map.of("estadoEsperado", "REU-PEND", "notas", "x".repeat(2001)), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.notas").exists());
        postear(ruta("/reunion/finalizar"), Map.of("estadoEsperado", "REU-PEND"), aspirante)
                .andExpect(status().isForbidden());
        postear(ruta("/reunion/finalizar"),
                        Map.of("estadoEsperado", "REU-PEND", "notas", "  Se acordó reconocer 6 materias  "),
                        academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("ANAL-POST"))
                .andExpect(jsonPath(
                        "$.accionesDisponibles",
                        containsInAnyOrder("ACEPTAR_INSCRIPCION", "RECHAZAR_INSCRIPCION", "ACTUALIZAR_ANALISIS")));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("la reunión se realizó"), conTexto("definir tu inscripción"));

        mvc.perform(get(ruta("/reunion")).cookie(academico))
                .andExpect(jsonPath("$.reunion.resultado").value("REALIZADA"))
                .andExpect(jsonPath("$.reunion.notas").value("Se acordó reconocer 6 materias"));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.reunion.resultado").value("REALIZADA"))
                .andExpect(jsonPath("$.reunion.notas").doesNotExist());
        // Las notas no son una observación para el aspirante.
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.ultimaObservacion").doesNotExist())
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
    }

    @Test
    void elAcademicoActualizaElAnalisisDespuesDeLaReunion() throws Exception {
        MockMultipartFile matriz = pdf("matriz-v2.pdf", 1024);
        // Antes de la reunión no se actualiza: se carga con la propuesta de fechas.
        actualizar(academico, "MATRIZ_EQUIVALENCIAS", matriz, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        finalizarReunion();

        actualizar(academico, "OTRA_COSA", matriz, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipo").exists());
        actualizar(academico, "MATRIZ_EQUIVALENCIAS", null, " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.notas").exists());
        actualizar(aspirante, "MATRIZ_EQUIVALENCIAS", matriz, null).andExpect(status().isForbidden());
        permisoCarrera(usuario("p.sosa@usal.edu.ar", "ACAD"), "ING-INF-001");
        actualizar(loginPersonal("p.sosa@usal.edu.ar"), "MATRIZ_EQUIVALENCIAS", matriz, null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));

        // La versión nueva reemplaza entera a la anterior: tenía notas y ahora es solo un archivo.
        actualizar(academico, "MATRIZ_EQUIVALENCIAS", matriz, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("MATRIZ_EQUIVALENCIAS"))
                .andExpect(jsonPath("$[0].version").value(2))
                .andExpect(jsonPath("$[0].nombreArchivo").value("matriz-v2.pdf"))
                .andExpect(jsonPath("$[0].notas").doesNotExist())
                .andExpect(jsonPath("$[1].tipo").value("HORARIO_CURSADA"))
                .andExpect(jsonPath("$[1].version").value(1));
        actualizar(academico, "HORARIO_CURSADA", null, "Turno mañana")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].version").value(2))
                .andExpect(jsonPath("$[1].notas").value("Turno mañana"));

        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.estado.codigo").value("ANAL-POST"));
        mvc.perform(get(ruta("/analisis")).cookie(aspirante))
                .andExpect(jsonPath("$[0].version").value(2))
                .andExpect(jsonPath("$[1].notas").value("Turno mañana"));
        // Las versiones anteriores quedan en la base, enlazadas, pero no salen por la API.
        assertThat(jdbc.queryForList(
                        "SELECT reemplaza_a_id FROM analisis_academico WHERE version = 2 ORDER BY id", Integer.class))
                .containsExactly(1, 2);
        verify(mail).enviarAviso(
                eq(ASPIRANTE), conTexto("se actualizó el análisis"), conTexto("actualizó la matriz de equivalencias"));
        verify(mail).enviarAviso(
                eq(ASPIRANTE), conTexto("se actualizó el análisis"), conTexto("actualizó el horario de cursada"));
        assertThat(filasAuditoria(ACADEMICO, "actualizar_analisis")).isEqualTo(2);
    }

    @Test
    void elAcademicoAceptaLaInscripcion() throws Exception {
        finalizarReunion();

        postear(ruta("/inscripcion/aceptar"), Map.of("estadoEsperado", "ANAL-POST"), aspirante)
                .andExpect(status().isForbidden());
        postear(ruta("/inscripcion/aceptar"), Map.of("estadoEsperado", "REU-PEND"), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        postear(ruta("/inscripcion/aceptar"), Map.of("estadoEsperado", "ANAL-POST"), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("INSC-ACEP"))
                .andExpect(jsonPath("$.estado.esTerminal").value(false))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("tu inscripción fue aceptada"), conTexto("legalizados"));
        // Ya decidida, no se vuelve atrás ni se sigue actualizando el análisis.
        postear(ruta("/inscripcion/rechazar"), Map.of("estadoEsperado", "ANAL-POST", "comentario", "x"), academico)
                .andExpect(status().isConflict());
        actualizar(academico, "HORARIO_CURSADA", null, "Turno mañana").andExpect(status().isConflict());
    }

    @Test
    void elRechazoDeLaInscripcionCierraElTramite() throws Exception {
        finalizarReunion();

        postear(ruta("/inscripcion/rechazar"), Map.of("estadoEsperado", "ANAL-POST", "comentario", " "), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta("/inscripcion/rechazar"),
                        Map.of("estadoEsperado", "ANAL-POST", "comentario", "x".repeat(501)),
                        academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta("/inscripcion/rechazar"),
                        Map.of("estadoEsperado", "ANAL-POST", "comentario", "El plan de origen no es compatible"),
                        academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("INSC-RECH"))
                .andExpect(jsonPath("$.estado.esTerminal").value(true))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));
        verify(mail).enviarAviso(
                eq(ASPIRANTE), conTexto("no pudo ser aprobada"), conTexto("Motivo: El plan de origen no es compatible"));

        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.ultimaObservacion.comentario").value("El plan de origen no es compatible"))
                .andExpect(jsonPath("$.accionesDisponibles", empty()))
                .andExpect(jsonPath("$.puedeEscribirChat").value(false));
        postear(ruta("/inscripcion/aceptar"), Map.of("estadoEsperado", "INSC-RECH"), academico)
                .andExpect(status().isConflict());
        // Con el trámite cerrado el chat queda en solo lectura.
        mvc.perform(multipart(ruta("/chat/mensajes"))
                        .param("contenido", "¿Puedo volver a presentarme?")
                        .param("tipoConsulta", "OTRO")
                        .cookie(aspirante))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        mvc.perform(get(ruta("/chat/mensajes")).cookie(aspirante)).andExpect(status().isOk());
    }

    private String ruta(String sufijo) {
        return "/solicitudes/" + solicitud + sufijo;
    }

    /** Dentro de `dias` días a la hora indicada de Argentina, como lo manda el frontend. */
    private static String horario(int dias, int hora) {
        return LocalDate.now(ZONA).plusDays(dias).atTime(hora, 0).atZone(ZONA).toOffsetDateTime().toString();
    }

    /** El aspirante elige un horario de la ronda vigente: la solicitud queda en `REU-PEND`. */
    private void pactarReunion(int horarioId) throws Exception {
        String estado = json.readTree(mvc.perform(get(ruta("")).cookie(aspirante))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .at("/estado/codigo")
                .asText();
        postear(ruta("/reunion/elegir-horario"), Map.of("estadoEsperado", estado, "horarioId", horarioId), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-PEND"));
    }

    private void segundaRonda(String... inicios) throws Exception {
        Map<String, Object> cuerpo = Map.of(
                "estadoEsperado",
                "REU-FEC-ACAD",
                "horarios",
                Arrays.stream(inicios).map(inicio -> Map.of("inicio", inicio)).toList());
        postear(ruta("/reunion/segunda-ronda"), cuerpo, academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ASP-2"));
    }

    /** Reunión pactada y realizada: la solicitud queda en `ANAL-POST`. */
    private void finalizarReunion() throws Exception {
        pactarReunion(1);
        postear(ruta("/reunion/finalizar"), Map.of("estadoEsperado", "REU-PEND"), academico)
                .andExpect(status().isOk());
    }

    private ResultActions actualizar(Cookie sesion, String tipo, MockMultipartFile archivo, String notas)
            throws Exception {
        MockMultipartHttpServletRequestBuilder pedido = multipart(ruta("/analisis"));
        pedido.param("tipo", tipo);
        if (archivo != null) {
            pedido.file(archivo);
        }
        if (notas != null) {
            pedido.param("notas", notas);
        }
        return mvc.perform(pedido.cookie(sesion));
    }
}
