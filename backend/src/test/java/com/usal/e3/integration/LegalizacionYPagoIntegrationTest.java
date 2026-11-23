package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

/**
 * RF-ASP-009, RF-ACAD-005, RF-ASP-006, RF-SEC-001, RF-ASP-007 y RF-SEC-002: la legalización (transiciones 16 a
 * 21), el informe de materias, el cupón, el comprobante y la verificación del pago (22 a 26).
 */
class LegalizacionYPagoIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final String SECRETARIA = "c.ruiz@usal.edu.ar";

    private Cookie aspirante;
    private Cookie academico;
    private Cookie secretaria;
    private int solicitud;

    /** La solicitud queda en `INSC-ACEP` con el Académico como responsable: el recorrido anterior ya tiene tests. */
    @BeforeEach
    void dejarLaInscripcionAceptada() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
        mvc.perform(post(ruta("/responsables/tomar")).cookie(academico)).andExpect(status().isOk());
        permisoCarrera(usuario(SECRETARIA, "SECRE"), "ING-INF-001");
        secretaria = loginPersonal(SECRETARIA);
        jdbc.update("UPDATE documentos_solicitud SET estado_revision = 'OK'");
        pasarA("INSC-ACEP");
    }

    @Test
    void elAspiranteIndicaQueNoEstanLegalizadosYDespuesAvisa() throws Exception {
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("INDICAR_LEGALIZACION")));
        postear(ruta("/legalizacion/indicar"), Map.of("estadoEsperado", "INSC-ACEP"), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.legalizados").exists());
        postear(ruta("/legalizacion/indicar"), Map.of("estadoEsperado", "INSC-ACEP", "legalizados", true), academico)
                .andExpect(status().isForbidden());

        postear(ruta("/legalizacion/indicar"), Map.of("estadoEsperado", "INSC-ACEP", "legalizados", false), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("LEG-PEND"))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("AVISAR_LEGALIZADO")));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("legalizá tus documentos"), conTexto("avisá"));

        postear(ruta("/legalizacion/listo"), Map.of("estadoEsperado", "INSC-ACEP"), aspirante)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        postear(ruta("/legalizacion/listo"), Map.of("estadoEsperado", "LEG-PEND"), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("LEG-REV"));
        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("documentos legalizados para analizar"), conTexto("EXP-"));

        // Los documentos se revisan de nuevo, ahora en la etapa de legalización.
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM documentos_solicitud"
                                + " WHERE estado_revision = 'PENDIENTE' AND etapa_revision = 'LEGALIZACION'",
                        Integer.class))
                .isEqualTo(3);
    }

    @Test
    void elAcademicoRechazaLosLegalizadosYElAspiranteLosRecarga() throws Exception {
        postear(ruta("/legalizacion/indicar"), Map.of("estadoEsperado", "INSC-ACEP", "legalizados", true), aspirante)
                .andExpect(jsonPath("$.estado.codigo").value("LEG-REV"));
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath(
                        "$.accionesDisponibles",
                        hasItems("ACEPTAR_LEGALIZADOS", "RECHAZAR_LEGALIZADOS", "GUARDAR_MARCAS", "AGREGAR_FALTANTE")));

        // Las mismas reglas que el rechazo inicial: motivo, al menos un ❌ y ninguno sin revisar.
        postear(ruta("/legalizacion/rechazar"), Map.of("estadoEsperado", "LEG-REV", "comentario", "Falta la firma"), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MARCAS_INCOMPLETAS"));
        postear(ruta("/legalizacion/rechazar"), Map.of("estadoEsperado", "LEG-REV", "marcas", marcas("RECARGAR")), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta("/legalizacion/rechazar"),
                        Map.of("estadoEsperado", "LEG-REV", "comentario", "Falta la firma", "marcas", marcas("RECARGAR")),
                        academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("LEG-RECH"));
        verify(mail)
                .enviarAviso(eq(ASPIRANTE), conTexto("documentos legalizados"), conTexto("Motivo: Falta la firma"));

        // Solo se reemplaza el marcado con ❌, y el envío vuelve a `LEG-REV`.
        recargar(aspirante, solicitud, 2, pdf("plan-legalizado.pdf", 1024)).andExpect(status().isConflict());
        postear(ruta("/documentacion/enviar"), Map.of("estadoEsperado", "LEG-RECH"), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documentos").exists());
        recargar(aspirante, solicitud, 1, pdf("analitico-legalizado.pdf", 1024)).andExpect(status().isCreated());
        postear(ruta("/documentacion/enviar"), Map.of("estadoEsperado", "LEG-RECH"), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("LEG-REV"));
        assertThat(jdbc.queryForMap(
                        "SELECT version, estado_revision, etapa_revision FROM documentos_solicitud WHERE reemplaza_a_id = 1"))
                .containsEntry("version", 2)
                .containsEntry("estado_revision", "PENDIENTE")
                .containsEntry("etapa_revision", "LEGALIZACION");
    }

    @Test
    void alAceptarLosLegalizadosSeGuardaElInformeDeMaterias() throws Exception {
        postear(ruta("/legalizacion/indicar"), Map.of("estadoEsperado", "INSC-ACEP", "legalizados", true), aspirante)
                .andExpect(status().isOk());
        mvc.perform(get(ruta("/informe")).cookie(aspirante)).andExpect(status().isNotFound());

        postear(ruta("/legalizacion/aceptar"), Map.of("estadoEsperado", "LEG-REV", "marcas", marcas("OK")), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.materiasReconocidas").exists());
        postear(ruta("/legalizacion/aceptar"),
                        Map.of("estadoEsperado", "LEG-REV", "marcas", marcas("RECARGAR"), "materiasReconocidas", 6),
                        academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.marcas").exists());
        postear(ruta("/legalizacion/aceptar"),
                        Map.of("estadoEsperado", "LEG-REV", "marcas", marcas("OK"), "materiasReconocidas", 6),
                        aspirante)
                .andExpect(status().isForbidden());

        postear(ruta("/legalizacion/aceptar"),
                        Map.of(
                                "estadoEsperado", "LEG-REV",
                                "marcas", marcas("OK"),
                                "materiasReconocidas", 6,
                                "observaciones", "Queda Física II para cursar."),
                        academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("CUPON-SOL"));
        // Nadie de Secretaría tomó el proceso todavía: el aviso va a quienes tienen acceso a la carrera.
        verify(mail).enviarAviso(eq(SECRETARIA), conTexto("lista para gestión de pago"), conTexto("cupón"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("fueron aceptados"), conTexto("Materias reconocidas: 6"));

        mvc.perform(get(ruta("/informe")).cookie(aspirante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiasReconocidas").value(6))
                .andExpect(jsonPath("$.observaciones").value("Queda Física II para cursar."))
                .andExpect(jsonPath("$.montoAPagar").value(nullValue()));
    }

    @Test
    void secretariaEnviaElCuponYVerificaElPago() throws Exception {
        pasarA("CUPON-SOL");
        Map<String, Object> cupon = Map.of("estadoEsperado", "CUPON-SOL", "monto", 150000.50);
        postear(ruta("/pago/cupon"), cupon, academico).andExpect(status().isForbidden());
        postear(ruta("/pago/cupon"), cupon, secretaria)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));
        mvc.perform(post(ruta("/responsables/tomar")).cookie(secretaria))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("ENVIAR_CUPON")));
        postear(ruta("/pago/cupon"), Map.of("estadoEsperado", "CUPON-SOL"), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.monto").exists());
        postear(ruta("/pago/cupon"), Map.of("estadoEsperado", "CUPON-SOL", "monto", 10.123), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.monto").exists());

        postear(ruta("/pago/cupon"), cupon, secretaria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("CUPON-PAG"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("cupón de pago está listo"), conTexto("comprobante"));
        mvc.perform(get(ruta("/pago")).cookie(aspirante))
                .andExpect(jsonPath("$.cupon.monto").value(150000.5))
                .andExpect(jsonPath("$.comprobantes").isEmpty());
        byte[] pdfDelCupon = mvc.perform(get(ruta("/pago/cupon/archivo")).cookie(aspirante))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(new String(pdfDelCupon, 0, 4)).isEqualTo("%PDF");

        // El comprobante: PDF o JPG. Secretaría lo rechaza con su motivo y el aspirante lo reenvía.
        comprobante("CUPON-PAG", new MockMultipartFile("archivo", "pago.txt", "text/plain", "hola".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ARCHIVO_INVALIDO"));
        comprobante("CUPON-PAG", pdf("pago.pdf", 2048))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("PAG-CTRL"));
        verify(mail).enviarAviso(eq(SECRETARIA), conTexto("comprobante de pago"), conTexto("EXP-"));
        postear(ruta("/pago/rechazar"), Map.of("estadoEsperado", "PAG-CTRL"), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta("/pago/rechazar"),
                        Map.of("estadoEsperado", "PAG-CTRL", "comentario", "El monto no coincide con el cupón."),
                        secretaria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("PAG-REENV"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("problema con tu comprobante"), conTexto("El monto no coincide"));
        mvc.perform(get(ruta("/pago")).cookie(aspirante))
                .andExpect(jsonPath("$.comprobantes[0].estadoVerificacion").value("RECHAZADO"))
                .andExpect(jsonPath("$.comprobantes[0].comentarioSecretaria").value("El monto no coincide con el cupón."));

        comprobante("PAG-REENV", pdf("pago-corregido.pdf", 2048))
                .andExpect(jsonPath("$.estado.codigo").value("PAG-CTRL"));
        postear(ruta("/pago/aceptar"), Map.of("estadoEsperado", "PAG-CTRL"), aspirante).andExpect(status().isForbidden());
        postear(ruta("/pago/aceptar"), Map.of("estadoEsperado", "PAG-CTRL"), secretaria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("E3-INI"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("tu pago fue verificado"), conTexto("verificado"));
        mvc.perform(get(ruta("/pago")).cookie(secretaria))
                .andExpect(jsonPath("$.comprobantes[1].version").value(2))
                .andExpect(jsonPath("$.comprobantes[1].estadoVerificacion").value("ACEPTADO"));
        mvc.perform(get(ruta("/pago/comprobantes/2/archivo")).cookie(secretaria)).andExpect(status().isOk());

        // El pago de otro aspirante no se ve.
        aspiranteValidado("otra@gmail.com", "40999888");
        mvc.perform(get(ruta("/pago")).cookie(loginAspirante("otra@gmail.com"))).andExpect(status().isNotFound());
    }

    private ResultActions comprobante(String estadoEsperado, MockMultipartFile archivo) throws Exception {
        return mvc.perform(multipart(ruta("/pago/comprobante"))
                .file(archivo)
                .param("estadoEsperado", estadoEsperado)
                .cookie(aspirante));
    }

    /** El primer documento con la marca indicada y los otros dos correctos. */
    private static List<Map<String, Object>> marcas(String primera) {
        return List.of(
                Map.of("documentoId", 1, "estadoRevision", primera, "comentario", "Revisado"),
                Map.of("documentoId", 2, "estadoRevision", "OK"),
                Map.of("documentoId", 3, "estadoRevision", "OK"));
    }

    private void pasarA(String estado) {
        jdbc.update("UPDATE solicitudes SET estado_codigo = ?", estado);
    }

    private String ruta(String resto) {
        return "/solicitudes/" + solicitud + resto;
    }
}
