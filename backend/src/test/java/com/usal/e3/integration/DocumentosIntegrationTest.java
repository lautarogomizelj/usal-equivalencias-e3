package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** RF-ASP-002, RF-ASP-010 y las transiciones 2 (`SOL-INI` → `DOC-REV`) y 27 (`SOL-INI` → `SOL-CANC`). */
class DocumentosIntegrationTest extends IntegracionBase {

    private static final String EMAIL = "juan.perez@gmail.com";
    private static final int MB = 1024 * 1024;

    private Cookie sesion;
    private int solicitud;

    @BeforeEach
    void prepararSolicitud() throws Exception {
        aspiranteValidado(EMAIL, "40123456");
        sesion = loginAspirante(EMAIL);
        solicitud = crearSolicitud(sesion);
    }

    @Test
    void cadaArchivoQuedaComoBorradorYSePuedeDescargar() throws Exception {
        MockMultipartFile archivo = pdf("analitico.pdf", 2048);

        subir(sesion, solicitud, "ANALITICO", archivo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ANALITICO"))
                .andExpect(jsonPath("$.nombreArchivo").value("analitico.pdf"))
                .andExpect(jsonPath("$.tamanoBytes").value(2048))
                .andExpect(jsonPath("$.borrador").value(true))
                .andExpect(jsonPath("$.estadoRevision").value("PENDIENTE"))
                .andExpect(jsonPath("$.version").value(1));

        String url = "/solicitudes/" + solicitud + "/documentos/1/archivo";
        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(sesion))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].urlDescarga").value(url));
        mvc.perform(get(url).cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("analitico.pdf")))
                .andExpect(content().bytes(archivo.getBytes()));
    }

    @Test
    void soloSeAceptanPdfYJpgPorSuContenido() throws Exception {
        MockMultipartFile disfrazado = new MockMultipartFile("archivo", "plan.pdf", "application/pdf", "hola".getBytes());
        MockMultipartFile jpg = new MockMultipartFile(
                "archivo", "plan.jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});

        subir(sesion, solicitud, "PLAN", disfrazado)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ARCHIVO_INVALIDO"));
        subir(sesion, solicitud, "PLAN", jpg).andExpect(status().isCreated());
    }

    @Test
    void unArchivoNoPuedeSuperarLos10Mb() throws Exception {
        subir(sesion, solicitud, "PROGRAMA", pdf("programa.pdf", 10 * MB + 1))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("LIMITE_TAMANO"));
    }

    @Test
    void entreTodosNoPuedenSuperarLos50Mb() throws Exception {
        for (int i = 0; i < 5; i++) {
            subir(sesion, solicitud, "PROGRAMA", pdf("programa" + i + ".pdf", 9 * MB))
                    .andExpect(status().isCreated());
        }

        subir(sesion, solicitud, "PROGRAMA", pdf("programa5.pdf", 9 * MB))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("LIMITE_TAMANO"));
    }

    @Test
    void elTipoTieneQueSerUnoDelContrato() throws Exception {
        subir(sesion, solicitud, "DNI", pdf("dni.pdf", 1024))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipo").exists());
    }

    @Test
    void elAspiranteQuitaUnBorrador() throws Exception {
        subir(sesion, solicitud, "OTRO", pdf("otro.pdf", 1024)).andExpect(status().isCreated());

        mvc.perform(delete("/solicitudes/" + solicitud + "/documentos/1").cookie(sesion))
                .andExpect(status().isNoContent());
        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(sesion))
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void sinLosObligatoriosNoSeEnvia() throws Exception {
        subir(sesion, solicitud, "ANALITICO", pdf("analitico.pdf", 1024)).andExpect(status().isCreated());

        postear(enviar(), Map.of("estadoEsperado", "SOL-INI"), sesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documentos[0]").value(containsString("el plan de estudios")));
        mvc.perform(get("/solicitudes/" + solicitud).cookie(sesion))
                .andExpect(jsonPath("$.estado.codigo").value("SOL-INI"));
    }

    @Test
    void alEnviarPasaADocRevYQuedaEnElHistorial() throws Exception {
        subirObligatorios();

        postear(enviar(), Map.of("estadoEsperado", "SOL-INI"), sesion)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("DOC-REV"))
                .andExpect(jsonPath("$.accionesDisponibles", empty()))
                .andExpect(jsonPath("$.resumenDocumentos.pendiente").value(3));

        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(sesion))
                .andExpect(jsonPath("$[*].borrador", everyItem(is(false))));
        mvc.perform(get("/solicitudes/" + solicitud + "/historial").cookie(sesion))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_ESTADO"))
                .andExpect(jsonPath("$[0].estadoAnterior").value("SOL-INI"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("DOC-REV"))
                .andExpect(jsonPath("$[0].usuario").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].tipo").value("DOCUMENTO_CARGADO"))
                .andExpect(jsonPath("$[4].estadoNuevo").value("SOL-INI"));
        assertThat(filasAuditoria(EMAIL, "cambio_estado")).isEqualTo(2);
    }

    @Test
    void loEnviadoYaNoSeCambia() throws Exception {
        enviarDocumentacion(sesion, solicitud);

        subir(sesion, solicitud, "OTRO", pdf("otro.pdf", 1024))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        mvc.perform(delete("/solicitudes/" + solicitud + "/documentos/1").cookie(sesion))
                .andExpect(status().isConflict());
        postear(enviar(), Map.of("estadoEsperado", "DOC-REV"), sesion)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void alCancelarQuedaCerradaYSeBorranLosBorradores() throws Exception {
        subir(sesion, solicitud, "ANALITICO", pdf("analitico.pdf", 1024)).andExpect(status().isCreated());

        postear(cancelar(), Map.of("estadoEsperado", "SOL-INI"), sesion)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("SOL-CANC"))
                .andExpect(jsonPath("$.estado.esTerminal").value(true))
                .andExpect(jsonPath("$.accionesDisponibles", empty()));

        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(sesion))
                .andExpect(jsonPath("$", empty()));
        mvc.perform(get("/solicitudes/" + solicitud + "/documentos/1/archivo").cookie(sesion))
                .andExpect(status().isNotFound());
        mvc.perform(get("/solicitudes/" + solicitud + "/historial").cookie(sesion))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estadoAnterior").value("SOL-INI"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("SOL-CANC"));
        mvc.perform(get("/solicitudes?finalizadas=true").cookie(sesion))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].requiereMiAccion").value(false));
        subir(sesion, solicitud, "OTRO", pdf("otro.pdf", 1024)).andExpect(status().isConflict());
        assertThat(filasAuditoria(EMAIL, "cambio_estado")).isEqualTo(2);
    }

    @Test
    void soloElDuenioCancelaYSoloAntesDeEnviar() throws Exception {
        aspiranteValidado("otra@gmail.com", "30111222");
        usuario("m.gomez@usal.edu.ar", "ACAD");

        postear(cancelar(), Map.of("estadoEsperado", "SOL-INI"), loginAspirante("otra@gmail.com"))
                .andExpect(status().isNotFound());
        postear(cancelar(), Map.of("estadoEsperado", "SOL-INI"), loginDesarrollo("m.gomez@usal.edu.ar"))
                .andExpect(status().isForbidden());

        enviarDocumentacion(sesion, solicitud);
        postear(cancelar(), Map.of("estadoEsperado", "DOC-REV"), sesion)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void conUnEstadoEsperadoViejoLaAccionSeRechaza() throws Exception {
        subirObligatorios();

        postear(enviar(), Map.of("estadoEsperado", "DOC-RECH"), sesion)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
        postear(enviar(), Map.of(), sesion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.estadoEsperado").exists());
    }

    @Test
    void elPersonalVeLoEnviadoPeroNoLosBorradores() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        Cookie personal = loginDesarrollo("m.gomez@usal.edu.ar");
        subir(sesion, solicitud, "ANALITICO", pdf("analitico.pdf", 1024)).andExpect(status().isCreated());
        String url = "/solicitudes/" + solicitud + "/documentos/1/archivo";

        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(personal))
                .andExpect(jsonPath("$", empty()));
        mvc.perform(get(url).cookie(personal)).andExpect(status().isNotFound());

        subir(sesion, solicitud, "PLAN", pdf("plan.pdf", 1024)).andExpect(status().isCreated());
        subir(sesion, solicitud, "PROGRAMA", pdf("programa.pdf", 1024)).andExpect(status().isCreated());
        postear(enviar(), Map.of("estadoEsperado", "SOL-INI"), sesion).andExpect(status().isOk());

        mvc.perform(get("/solicitudes/" + solicitud + "/documentos").cookie(personal))
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get(url).cookie(personal)).andExpect(status().isOk());
    }

    @Test
    void elPersonalNoSubeNiEnviaDocumentos() throws Exception {
        usuario("m.gomez@usal.edu.ar", "ACAD");
        Cookie personal = loginDesarrollo("m.gomez@usal.edu.ar");

        subir(personal, solicitud, "ANALITICO", pdf("analitico.pdf", 1024)).andExpect(status().isForbidden());
        postear(enviar(), Map.of("estadoEsperado", "SOL-INI"), personal).andExpect(status().isForbidden());
    }

    private String enviar() {
        return "/solicitudes/" + solicitud + "/documentacion/enviar";
    }

    private String cancelar() {
        return "/solicitudes/" + solicitud + "/cancelar";
    }

    private void subirObligatorios() throws Exception {
        for (String tipo : new String[] {"ANALITICO", "PLAN", "PROGRAMA"}) {
            subir(sesion, solicitud, tipo, pdf(tipo + ".pdf", 1024)).andExpect(status().isCreated());
        }
    }
}
