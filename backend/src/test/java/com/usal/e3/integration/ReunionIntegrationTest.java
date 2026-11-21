package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

/**
 * RF-ACAD-002, RF-ACAD-003 y RF-ASP-005: análisis preliminar con propuesta de fechas (transición 6), las dos
 * rondas (7 a 10) y el enlace de Meet. Los horarios quedan con los ids en el orden en que se proponen.
 */
class ReunionIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final String MEET = "https://meet.google.com/abc-defg-hij";
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private Cookie aspirante;
    private Cookie academico;
    private int solicitud;

    @BeforeEach
    void dejarLaSolicitudEnAnalisisPreliminar() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
        mvc.perform(post(ruta("/responsables/tomar")).cookie(academico)).andExpect(status().isOk());
        postear(ruta("/documentacion/aceptar"), Map.of("estadoEsperado", "DOC-REV"), academico)
                .andExpect(status().isOk());
    }

    @Test
    void elAcademicoCargaElAnalisisYProponeFechas() throws Exception {
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("PROPONER_FECHAS")));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.rondaActual").doesNotExist())
                .andExpect(jsonPath("$.rondas", hasSize(0)));

        // Se proponen desordenados: salen del más cercano al más lejano.
        proponer(academico, archivo("matrizArchivo", "matriz.pdf"), "  Se reconocen 6 materias  ", null, "Turno noche",
                        horario(5, 15), horario(2, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ASP-1"));

        mvc.perform(get(ruta("/analisis")).cookie(aspirante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("MATRIZ_EQUIVALENCIAS"))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[0].nombreArchivo").value("matriz.pdf"))
                .andExpect(jsonPath("$[0].notas").value("Se reconocen 6 materias"))
                .andExpect(jsonPath("$[0].subidoPor").value("María Gómez"))
                .andExpect(jsonPath("$[0].urlDescarga").value("/solicitudes/" + solicitud + "/analisis/1/archivo"))
                .andExpect(jsonPath("$[1].tipo").value("HORARIO_CURSADA"))
                .andExpect(jsonPath("$[1].notas").value("Turno noche"))
                .andExpect(jsonPath("$[1].nombreArchivo").doesNotExist())
                .andExpect(jsonPath("$[1].urlDescarga").doesNotExist());
        mvc.perform(get(ruta("/analisis/1/archivo")).cookie(aspirante)).andExpect(status().isOk());
        // El horario se cargó solo con notas: no tiene archivo.
        mvc.perform(get(ruta("/analisis/2/archivo")).cookie(aspirante)).andExpect(status().isNotFound());

        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.rondaActual").value(1))
                .andExpect(jsonPath("$.rondas[0].horarios[*].id", contains(2, 1)))
                .andExpect(jsonPath("$.rondas[0].horarios[0].elegido").value(false))
                .andExpect(jsonPath("$.reunion").doesNotExist());
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath(
                        "$.accionesDisponibles", containsInAnyOrder("ELEGIR_HORARIO", "SOLICITAR_OTRAS_FECHAS")));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("elegí un horario"), conTexto("Horarios propuestos: "));

        aspiranteValidado("otra@gmail.com", "40999888");
        Cookie ajeno = loginAspirante("otra@gmail.com");
        mvc.perform(get(ruta("/analisis")).cookie(ajeno)).andExpect(status().isNotFound());
        mvc.perform(get(ruta("/analisis/1/archivo")).cookie(ajeno)).andExpect(status().isNotFound());
        mvc.perform(get(ruta("/reunion")).cookie(ajeno)).andExpect(status().isNotFound());
    }

    @Test
    void laPropuestaExigeMatrizHorarioYFechasValidas() throws Exception {
        String valido = horario(3, 11);
        MockMultipartFile matriz = archivo("matrizArchivo", "matriz.pdf");

        proponer(academico, null, " ", null, "Turno noche", valido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.matriz").exists());
        proponer(academico, matriz, null, null, null, valido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.horario").exists());
        proponer(academico, matriz, null, null, "x".repeat(2001), valido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.horario").exists());

        String[] ocho = new String[8];
        Arrays.setAll(ocho, i -> horario(2 + i, 10));
        List<String[]> fechasInvalidas = List.of(
                new String[] {},
                ocho,
                new String[] {horario(0, 17)}, // hoy
                new String[] {horario(31, 10)}, // más de 30 días
                new String[] {horario(3, 9)}, // antes de las 10
                new String[] {horario(3, 18)}, // terminaría a las 19
                new String[] {horario(3, 10).replace("T10:00", "T10:30")}, // no empieza en punto
                new String[] {valido, valido});
        for (String[] fechas : fechasInvalidas) {
            proponer(academico, matriz, null, null, "Turno noche", fechas)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.horarios").exists());
        }

        MockMultipartFile word = new MockMultipartFile("horarioArchivo", "horario.docx", "application/pdf", "PK".getBytes());
        proponer(academico, matriz, null, word, null, valido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ARCHIVO_INVALIDO"));

        // Nada quedó a medias: ni el estado, ni la ronda, ni la matriz que sí era válida.
        mvc.perform(get(ruta("")).cookie(academico)).andExpect(jsonPath("$.estado.codigo").value("ANAL-PEND"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM analisis_academico", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rondas_reunion", Integer.class)).isZero();

        // Los límites sí entran: mañana a las 10 y dentro de 30 días a las 17.
        proponer(academico, matriz, null, null, "Turno noche", horario(1, 10), horario(30, 17))
                .andExpect(status().isOk());
    }

    @Test
    void soloElResponsableProponeYConElEstadoQueVe() throws Exception {
        permisoCarrera(usuario("p.sosa@usal.edu.ar", "ACAD"), "ING-INF-001");
        MockMultipartFile matriz = archivo("matrizArchivo", "matriz.pdf");

        proponer(loginPersonal("p.sosa@usal.edu.ar"), matriz, null, null, "Turno noche", horario(3, 11))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));
        proponer(aspirante, matriz, null, null, "Turno noche", horario(3, 11)).andExpect(status().isForbidden());
        mvc.perform(propuesta("DOC-REV", matriz, null, null, "Turno noche", horario(3, 11)).cookie(academico))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        // El enlace de Meet se carga recién con la reunión pactada.
        enviar(put(ruta("/reunion/enlace")), Map.of("enlaceMeet", MEET), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void elAspiranteEligeUnHorarioYElAcademicoCargaElMeet() throws Exception {
        proponer(academico, archivo("matrizArchivo", "matriz.pdf"), null, archivo("horarioArchivo", "horario.pdf"), null,
                        horario(2, 10), horario(5, 15))
                .andExpect(status().isOk());

        postear(ruta("/reunion/elegir-horario"), Map.of("estadoEsperado", "REU-FEC-ASP-1", "horarioId", 99), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.horarioId").exists());
        postear(ruta("/reunion/elegir-horario"), Map.of("estadoEsperado", "REU-FEC-ASP-1", "horarioId", 2), academico)
                .andExpect(status().isForbidden());
        postear(ruta("/reunion/elegir-horario"), Map.of("estadoEsperado", "REU-FEC-ASP-1", "horarioId", 2), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-PEND"))
                .andExpect(jsonPath("$.accionesDisponibles", hasSize(0)));
        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("eligió la fecha"), conTexto("Incluí el enlace de Meet"));

        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.rondas[0].horarios[*].elegido", contains(false, true)))
                .andExpect(jsonPath("$.reunion.origen").value("ELEGIDA_ASPIRANTE"))
                .andExpect(jsonPath("$.reunion.resultado").value("PENDIENTE"))
                .andExpect(jsonPath("$.reunion.enlaceMeet").doesNotExist());
        assertThat(jdbc.queryForObject(
                        "SELECT r.inicio = h.inicio AND r.fin = h.inicio + INTERVAL '1 hour' FROM reuniones r"
                                + " JOIN horarios_propuestos h ON h.id = r.horario_propuesto_id",
                        Boolean.class))
                .isTrue();

        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("CARGAR_ENLACE_MEET")));
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.accionesDisponibles", not(hasItem("CARGAR_ENLACE_MEET"))));
        for (String invalido : List.of("meet.google.com/abc", "http://meet.google.com/abc", "https://meet.google.com/a b")) {
            enviar(put(ruta("/reunion/enlace")), Map.of("enlaceMeet", invalido), academico)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.enlaceMeet").exists());
        }
        enviar(put(ruta("/reunion/enlace")), Map.of("enlaceMeet", MEET), aspirante).andExpect(status().isForbidden());
        enviar(put(ruta("/reunion/enlace")), Map.of("enlaceMeet", "  " + MEET + " "), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reunion.enlaceMeet").value(MEET));

        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.estado.codigo").value("REU-PEND"));
        mvc.perform(get(ruta("/reunion")).cookie(aspirante)).andExpect(jsonPath("$.reunion.enlaceMeet").value(MEET));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("tu reunión fue confirmada"), conTexto("Link: " + MEET));
        assertThat(filasAuditoria(ACADEMICO, "enlace_meet")).isEqualTo(1);
    }

    @Test
    void elAspirantePideOtrasFechasYRechazaLaSegundaRonda() throws Exception {
        proponer(academico, null, "Se reconocen 6 materias", null, "Turno noche", horario(2, 10))
                .andExpect(status().isOk());

        Map<String, Object> pedido = Map.of(
                "estadoEsperado", "REU-FEC-ASP-1",
                "motivo", "Trabajo en ese horario",
                "disponibilidad", "Martes y jueves después de las 16");
        postear(ruta("/reunion/solicitar-otras-fechas"), Map.of("estadoEsperado", "REU-FEC-ASP-1", "motivo", " "), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.motivo").exists());
        postear(ruta("/reunion/solicitar-otras-fechas"),
                        Map.of("estadoEsperado", "REU-FEC-ASP-1", "motivo", "Trabajo en ese horario"), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.disponibilidad").exists());
        // La 2ª ronda no se rechaza desde la 1ª.
        postear(ruta("/reunion/rechazar-ronda"), Map.of("estadoEsperado", "REU-FEC-ASP-1"), aspirante)
                .andExpect(status().isConflict());
        postear(ruta("/reunion/solicitar-otras-fechas"), pedido, aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ACAD"))
                .andExpect(jsonPath("$.accionesDisponibles", hasSize(0)));
        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("pidió otras fechas"), conTexto("Trabajo en ese horario"));

        mvc.perform(get(ruta("/reunion")).cookie(academico))
                .andExpect(jsonPath("$.rondas[0].motivoOtrasFechas").value("Trabajo en ese horario"))
                .andExpect(jsonPath("$.rondas[0].disponibilidadAspirante").value("Martes y jueves después de las 16"));
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("PROPONER_SEGUNDA_RONDA")));

        postear(ruta("/reunion/segunda-ronda"), horarios("REU-FEC-ACAD"), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.horarios").exists());
        postear(ruta("/reunion/segunda-ronda"), horarios("REU-FEC-ACAD", horario(4, 16), horario(6, 17)), aspirante)
                .andExpect(status().isForbidden());
        postear(ruta("/reunion/segunda-ronda"), horarios("REU-FEC-ACAD", horario(4, 16), horario(6, 17)), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-FEC-ASP-2"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("segunda ronda"), conTexto("Horarios propuestos: "));

        mvc.perform(get(ruta("/reunion")).cookie(aspirante))
                .andExpect(jsonPath("$.rondaActual").value(2))
                .andExpect(jsonPath("$.rondas[1].horarios[*].id", contains(2, 3)));
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath(
                        "$.accionesDisponibles", containsInAnyOrder("ELEGIR_HORARIO", "RECHAZAR_SEGUNDA_RONDA")));
        // No hay 3ª ronda, y los horarios de la 1ª ya no se eligen.
        postear(ruta("/reunion/solicitar-otras-fechas"),
                        Map.of("estadoEsperado", "REU-FEC-ASP-2", "motivo", "x", "disponibilidad", "y"), aspirante)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        postear(ruta("/reunion/elegir-horario"), Map.of("estadoEsperado", "REU-FEC-ASP-2", "horarioId", 1), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.horarioId").exists());

        postear(ruta("/reunion/rechazar-ronda"), Map.of("estadoEsperado", "REU-FEC-ASP-2", "motivo", "Viajo esa semana"), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("REU-CHAT"));
        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("se pauta por chat"), conTexto("2ª ronda"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("se pauta por chat"), conTexto("2ª ronda"));
        assertThat(jdbc.queryForObject("SELECT rechazada FROM rondas_reunion WHERE numero = 2", Boolean.class))
                .isTrue();

        mvc.perform(get(ruta("/historial")).cookie(aspirante))
                .andExpect(jsonPath("$[0].estadoNuevo").value("REU-CHAT"))
                .andExpect(jsonPath("$[0].comentario").value("Viajo esa semana"))
                .andExpect(jsonPath("$[0].usuario").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("REU-FEC-ASP-2"))
                .andExpect(jsonPath("$[2].estadoNuevo").value("REU-FEC-ACAD"));
        // Lo que escribe el aspirante no es una observación del personal.
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.ultimaObservacion").doesNotExist());
    }

    private String ruta(String sufijo) {
        return "/solicitudes/" + solicitud + sufijo;
    }

    /** Dentro de `dias` días a la hora indicada de Argentina, como lo manda el frontend. */
    private static String horario(int dias, int hora) {
        return LocalDate.now(ZONA).plusDays(dias).atTime(hora, 0).atZone(ZONA).toOffsetDateTime().toString();
    }

    private static MockMultipartFile archivo(String parte, String nombre) {
        byte[] contenido = new byte[1024];
        System.arraycopy("%PDF-1.7".getBytes(), 0, contenido, 0, 8);
        return new MockMultipartFile(parte, nombre, "application/pdf", contenido);
    }

    private Map<String, Object> horarios(String estadoEsperado, String... inicios) {
        return Map.of(
                "estadoEsperado",
                estadoEsperado,
                "horarios",
                Arrays.stream(inicios).map(inicio -> Map.of("inicio", inicio)).toList());
    }

    private MockMultipartHttpServletRequestBuilder propuesta(
            String estadoEsperado,
            MockMultipartFile matriz,
            String matrizNotas,
            MockMultipartFile horarioCursada,
            String horarioNotas,
            String... inicios)
            throws Exception {
        MockMultipartHttpServletRequestBuilder pedido = multipart(ruta("/analisis/propuesta"));
        pedido.file(new MockMultipartFile(
                "datos", "", "application/json", json.writeValueAsBytes(horarios(estadoEsperado, inicios))));
        if (matriz != null) {
            pedido.file(matriz);
        }
        if (horarioCursada != null) {
            pedido.file(horarioCursada);
        }
        if (matrizNotas != null) {
            pedido.param("matrizNotas", matrizNotas);
        }
        if (horarioNotas != null) {
            pedido.param("horarioNotas", horarioNotas);
        }
        return pedido;
    }

    private ResultActions proponer(
            Cookie sesion,
            MockMultipartFile matriz,
            String matrizNotas,
            MockMultipartFile horarioCursada,
            String horarioNotas,
            String... inicios)
            throws Exception {
        return mvc.perform(
                propuesta("ANAL-PEND", matriz, matrizNotas, horarioCursada, horarioNotas, inicios).cookie(sesion));
    }
}
