package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * RF-ACAD-001, RF-ACAD-009 y RF-ASP-004: revisión por documento, aceptar o rechazar (transiciones 3 y 5) y
 * recarga selectiva (transición 4). Los documentos enviados quedan con los ids 1 (analítico), 2 (plan) y 3 (programa).
 */
class RevisionIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final int ANALITICO = 1;
    private static final int PLAN = 2;
    private static final int PROGRAMA = 3;
    private static final int MB = 1024 * 1024;

    private Cookie aspirante;
    private Cookie academico;
    private int solicitud;

    @BeforeEach
    void dejarLaSolicitudEnRevision() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
        mvc.perform(post(ruta("/responsables/tomar")).cookie(academico)).andExpect(status().isOk());
    }

    @Test
    void soloElAcademicoResponsableRevisa() throws Exception {
        permisoCarrera(usuario("p.sosa@usal.edu.ar", "ACAD"), "ING-INF-001");
        usuario("observador@usal.edu.ar", "ACAD");
        permisoCarrera(usuario("c.ruiz@usal.edu.ar", "SECRE"), "ING-INF-001");
        Map<String, Object> marcas = marcas(null, marca(ANALITICO, "OK", null));

        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath(
                        "$.accionesDisponibles",
                        containsInAnyOrder(
                                "ACEPTAR_DOCUMENTACION",
                                "RECHAZAR_DOCUMENTACION",
                                "GUARDAR_MARCAS",
                                "AGREGAR_FALTANTE",
                                "DARSE_DE_BAJA")));
        Map<String, String> sinResponsabilidad =
                Map.of("p.sosa@usal.edu.ar", "NO_RESPONSABLE", "observador@usal.edu.ar", "NO_PARTICIPANTE");
        for (var caso : sinResponsabilidad.entrySet()) {
            Cookie sesion = loginPersonal(caso.getKey());
            guardarMarcas(sesion, marcas)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(caso.getValue()));
            postear(ruta("/documentacion/aceptar"), marcas, sesion)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(caso.getValue()));
            postear(ruta("/documentos/faltantes"), Map.of("tipo", "OTRO", "descripcion", "DNI"), sesion)
                    .andExpect(status().isForbidden());
        }
        guardarMarcas(loginPersonal("c.ruiz@usal.edu.ar"), marcas).andExpect(status().isForbidden());
        guardarMarcas(aspirante, marcas).andExpect(status().isForbidden());
    }

    @Test
    void elAdminRevisaSinSerParticipanteNiResponsable() throws Exception {
        Cookie admin = loginAdmin(admin("admin@usal.edu.ar").getEmail());

        mvc.perform(get(ruta("")).cookie(admin))
                .andExpect(jsonPath("$.miRelacion").value("ADMIN"))
                .andExpect(jsonPath(
                        "$.accionesDisponibles",
                        containsInAnyOrder(
                                "ACEPTAR_DOCUMENTACION", "RECHAZAR_DOCUMENTACION", "GUARDAR_MARCAS", "AGREGAR_FALTANTE")));
        // Las del aspirante no entran en el bypass.
        postear(ruta("/documentacion/enviar"), Map.of("estadoEsperado", "DOC-REV"), admin)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        Map<String, Object> viejo = marcas(null);
        viejo.put("estadoEsperado", "SOL-INI");
        postear(ruta("/documentacion/aceptar"), viejo, admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));

        guardarMarcas(admin, marcas(null, marca(ANALITICO, "OK", null))).andExpect(status().isOk());
        postear(ruta("/documentacion/aceptar"), marcas(null), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("ANAL-PEND"))
                .andExpect(jsonPath("$.responsableAcademico.email").value(ACADEMICO));

        mvc.perform(get(ruta("/historial")).cookie(aspirante))
                .andExpect(jsonPath("$[0].estadoNuevo").value("ANAL-PEND"))
                .andExpect(jsonPath("$[0].usuario").value("Admin General"));
        assertThat(filasAuditoria("admin@usal.edu.ar", "cambio_estado")).isEqualTo(1);
    }

    @Test
    void deDosDecisionesALaVezSoloUnaCambiaElEstado() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> intentos = List.of(
                    () -> postear(ruta("/documentacion/aceptar"), marcas(null), academico)
                            .andReturn()
                            .getResponse()
                            .getStatus(),
                    () -> postear(ruta("/documentacion/rechazar"), marcas("El plan no es el vigente", todasRevisadas()), academico)
                            .andReturn()
                            .getResponse()
                            .getStatus());
            List<Integer> respuestas = new ArrayList<>();
            for (Future<Integer> intento : hilos.invokeAll(intentos)) {
                respuestas.add(intento.get());
            }
            assertThat(respuestas).containsExactlyInAnyOrder(200, 409);
        } finally {
            hilos.shutdown();
        }
        assertThat(filasAuditoria(ACADEMICO, "cambio_estado")).isEqualTo(1);
    }

    @Test
    void guardarMarcasNoCambiaElEstadoYQuedaEnAuditoria() throws Exception {
        Map<String, Object> marcas =
                marcas(null, marca(ANALITICO, "OK", null), marca(PLAN, "RECARGAR", "  Falta la firma  "));

        guardarMarcas(academico, marcas)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].estadoRevision", contains("OK", "RECARGAR", "PENDIENTE")))
                .andExpect(jsonPath("$[0].revisadoPor").value("María Gómez"))
                .andExpect(jsonPath("$[0].fechaRevision").isNotEmpty())
                .andExpect(jsonPath("$[1].comentarioRevision").value("Falta la firma"));

        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.estado.codigo").value("DOC-REV"))
                .andExpect(jsonPath("$.resumenDocumentos.ok").value(1))
                .andExpect(jsonPath("$.resumenDocumentos.recargar").value(1))
                .andExpect(jsonPath("$.resumenDocumentos.pendiente").value(1));
        assertThat(filasAuditoria(ACADEMICO, "marca_documento")).isEqualTo(2);

        guardarMarcas(academico, marcas).andExpect(status().isOk());
        assertThat(filasAuditoria(ACADEMICO, "marca_documento")).isEqualTo(2);
    }

    @Test
    void lasMarcasSeValidan() throws Exception {
        guardarMarcas(academico, marcas(null, marca(99, "OK", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.marcas").exists());
        guardarMarcas(academico, marcas(null, marca(ANALITICO, "CUALQUIERA", null)))
                .andExpect(status().isBadRequest());
        guardarMarcas(academico, marcas(null, marca(ANALITICO, "RECARGAR", "x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.marcas").exists());
        Map<String, Object> viejas = marcas(null, marca(ANALITICO, "OK", null));
        viejas.put("estadoEsperado", "SOL-INI");
        guardarMarcas(academico, viejas)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void elFaltanteQuedaSinArchivoYMarcadoParaRecargar() throws Exception {
        postear(ruta("/documentos/faltantes"), Map.of("tipo", "PROGRAMA", "descripcion", "Análisis Matemático II"), academico)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.tipo").value("PROGRAMA"))
                .andExpect(jsonPath("$.descripcion").value("Análisis Matemático II"))
                .andExpect(jsonPath("$.esRequeridoFaltante").value(true))
                .andExpect(jsonPath("$.estadoRevision").value("RECARGAR"))
                .andExpect(jsonPath("$.borrador").value(false))
                .andExpect(jsonPath("$.nombreArchivo").doesNotExist())
                .andExpect(jsonPath("$.urlDescarga").doesNotExist());

        postear(ruta("/documentos/faltantes"), Map.of("tipo", "PROGRAMA", "descripcion", " "), academico)
                .andExpect(status().isBadRequest());
        postear(ruta("/documentos/faltantes"), Map.of("tipo", "DNI", "descripcion", "Frente y dorso"), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipo").exists());
        guardarMarcas(academico, marcas(null, marca(4, "OK", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.marcas").exists());
        mvc.perform(get(ruta("/historial")).cookie(academico))
                .andExpect(jsonPath("$[?(@.tipo == 'DOCUMENTO_CARGADO')]", hasSize(3)));
    }

    @Test
    void alAceptarPasaAAnalisisYSeAvisaAlAspirante() throws Exception {
        postear(ruta("/documentacion/aceptar"), marcas(null, marca(ANALITICO, "OK", null)), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("ANAL-PEND"))
                .andExpect(jsonPath("$.resumenDocumentos.ok").value(1))
                .andExpect(jsonPath("$.accionesDisponibles", contains("PROPONER_FECHAS")));

        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("fue aceptada"), anyString());
        mvc.perform(get(ruta("/historial")).cookie(aspirante))
                .andExpect(jsonPath("$[0].estadoAnterior").value("DOC-REV"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("ANAL-PEND"))
                .andExpect(jsonPath("$[0].usuario").value("María Gómez"));
        postear(ruta("/documentacion/aceptar"), marcas(null), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"));
    }

    @Test
    void paraRechazarHacenFaltaElMotivoUnaCruzYNingunPendiente() throws Exception {
        postear(ruta("/documentacion/rechazar"), marcas(" ", todasRevisadas()), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta("/documentacion/rechazar"), marcas("Falta la firma", marca(PLAN, "RECARGAR", null)), academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MARCAS_INCOMPLETAS"));
        postear(
                        ruta("/documentacion/rechazar"),
                        marcas("Todo bien", marca(ANALITICO, "OK", null), marca(PLAN, "OK", null), marca(PROGRAMA, "OK", null)),
                        academico)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MARCAS_INCOMPLETAS"));

        // Un rechazo que no pasa no deja marcas guardadas.
        mvc.perform(get(ruta("")).cookie(academico))
                .andExpect(jsonPath("$.estado.codigo").value("DOC-REV"))
                .andExpect(jsonPath("$.resumenDocumentos.pendiente").value(3));
    }

    @Test
    void alRechazarElAspiranteRecibeElMotivoYLosDocumentosARecargar() throws Exception {
        rechazar();

        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.estado.codigo").value("DOC-RECH"))
                .andExpect(jsonPath("$.ultimaObservacion.comentario").value("El plan no es el vigente"))
                .andExpect(jsonPath("$.ultimaObservacion.usuario").value("María Gómez"))
                .andExpect(jsonPath("$.resumenDocumentos.recargar").value(1))
                .andExpect(jsonPath("$.accionesDisponibles", contains("ENVIAR_DOCUMENTACION")));
        mvc.perform(get(ruta("")).cookie(academico)).andExpect(jsonPath("$.accionesDisponibles", empty()));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("observaciones"), conTexto("Motivo: El plan no es el vigente"));
        verify(mail).enviarAviso(eq(ASPIRANTE), anyString(), conTexto("Documentos a recargar: Plan de estudios."));
        assertThat(filasAuditoria(ACADEMICO, "cambio_estado")).isEqualTo(1);
    }

    @Test
    void enLaRecargaSoloSeReemplazaLoMarcadoConCruz() throws Exception {
        rechazar();

        subir(aspirante, solicitud, "OTRO", pdf("otro.pdf", 1024))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.reemplazaAId").exists());
        recargar(aspirante, solicitud, ANALITICO, pdf("analitico-v2.pdf", 1024))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DOCUMENTO_BLOQUEADO"));
        recargar(aspirante, solicitud, 99, pdf("nada.pdf", 1024)).andExpect(status().isNotFound());
        postear(ruta("/documentacion/enviar"), Map.of("estadoEsperado", "DOC-RECH"), aspirante)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documentos[0]").value("Falta recargar: Plan de estudios."));

        recargar(aspirante, solicitud, PLAN, pdf("plan-viejo.pdf", 1024)).andExpect(status().isCreated());
        recargar(aspirante, solicitud, PLAN, pdf("plan-2026.pdf", 2048))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PLAN"))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.reemplazaAId").value(PLAN))
                .andExpect(jsonPath("$.borrador").value(true))
                .andExpect(jsonPath("$.estadoRevision").value("PENDIENTE"));

        // El aspirante ve el documento observado y su reemplazo; el personal, solo lo enviado.
        mvc.perform(get(ruta("/documentos")).cookie(aspirante))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].nombreArchivo", contains("analitico.pdf", "plan.pdf", "programa.pdf", "plan-2026.pdf")));
        mvc.perform(get(ruta("/documentos")).cookie(academico))
                .andExpect(jsonPath("$[*].estadoRevision", contains("OK", "RECARGAR", "OK")));
    }

    @Test
    void alReenviarVuelveARevisionConLaVersionNuevaPendiente() throws Exception {
        postear(ruta("/documentos/faltantes"), Map.of("tipo", "PROGRAMA", "descripcion", "Física I"), academico)
                .andExpect(status().isCreated());
        rechazar();
        recargar(aspirante, solicitud, PLAN, pdf("plan-2026.pdf", 2048)).andExpect(status().isCreated());
        recargar(aspirante, solicitud, 4, pdf("fisica.pdf", 1024))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PROGRAMA"))
                .andExpect(jsonPath("$.descripcion").value("Física I"))
                .andExpect(jsonPath("$.version").value(1));

        postear(ruta("/documentacion/enviar"), Map.of("estadoEsperado", "DOC-RECH"), aspirante)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("DOC-REV"))
                .andExpect(jsonPath("$.resumenDocumentos.ok").value(2))
                .andExpect(jsonPath("$.resumenDocumentos.pendiente").value(2))
                .andExpect(jsonPath("$.resumenDocumentos.recargar").value(0));

        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("recargada"), conTexto("recargó documentación"));
        mvc.perform(get(ruta("/documentos")).cookie(academico))
                .andExpect(jsonPath("$[*].nombreArchivo", contains("analitico.pdf", "programa.pdf", "plan-2026.pdf", "fisica.pdf")))
                .andExpect(jsonPath("$[*].estadoRevision", contains("OK", "OK", "PENDIENTE", "PENDIENTE")))
                .andExpect(jsonPath("$[2].version").value(2));
        mvc.perform(get(ruta("/documentos?versiones=true")).cookie(academico)).andExpect(jsonPath("$", hasSize(6)));

        // Criterio del hito: SOL-INI → DOC-REV → DOC-RECH → DOC-REV → ANAL-PEND.
        postear(ruta("/documentacion/aceptar"), marcas(null), academico)
                .andExpect(jsonPath("$.estado.codigo").value("ANAL-PEND"));
        mvc.perform(get(ruta("/historial")).cookie(aspirante))
                .andExpect(jsonPath(
                        "$[?(@.tipo == 'CAMBIO_ESTADO')].estadoNuevo",
                        contains("ANAL-PEND", "DOC-REV", "DOC-RECH", "DOC-REV", "SOL-INI")));
    }

    @Test
    void siLaRecargaSuperaLos50MbElBorradorAnteriorNoSePierde() throws Exception {
        for (int i = 1; i <= 5; i++) {
            postear(ruta("/documentos/faltantes"), Map.of("tipo", "PROGRAMA", "descripcion", "Materia " + i), academico)
                    .andExpect(status().isCreated());
        }
        rechazar();
        for (int faltante = 4; faltante <= 8; faltante++) {
            recargar(aspirante, solicitud, faltante, pdf("programa" + faltante + ".pdf", 9 * MB))
                    .andExpect(status().isCreated());
        }
        String respuesta = recargar(aspirante, solicitud, PLAN, pdf("plan-2026.pdf", 2 * MB))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        int borrador = json.readTree(respuesta).get("id").asInt();

        recargar(aspirante, solicitud, PLAN, pdf("plan-pesado.pdf", 6 * MB))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("LIMITE_TAMANO"));

        mvc.perform(get(ruta("/documentos/" + borrador + "/archivo")).cookie(aspirante))
                .andExpect(status().isOk());
    }

    /** Deja la solicitud en `DOC-RECH` con el plan marcado para recargar y el resto correcto. */
    private void rechazar() throws Exception {
        postear(ruta("/documentacion/rechazar"), marcas("El plan no es el vigente", todasRevisadas()), academico)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("DOC-RECH"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] todasRevisadas() {
        return new Map[] {
            marca(ANALITICO, "OK", null), marca(PLAN, "RECARGAR", "No es el plan vigente"), marca(PROGRAMA, "OK", null)
        };
    }

    private ResultActions guardarMarcas(Cookie sesion, Map<String, Object> cuerpo) throws Exception {
        return enviar(put(ruta("/documentos/marcas")), cuerpo, sesion);
    }

    private String ruta(String resto) {
        return "/solicitudes/" + solicitud + resto;
    }

    @SafeVarargs
    private static Map<String, Object> marcas(String comentario, Map<String, Object>... marcas) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("estadoEsperado", "DOC-REV");
        cuerpo.put("comentario", comentario);
        cuerpo.put("marcas", List.of(marcas));
        return cuerpo;
    }

    private static Map<String, Object> marca(int documentoId, String estado, String comentario) {
        Map<String, Object> marca = new HashMap<>();
        marca.put("documentoId", documentoId);
        marca.put("estadoRevision", estado);
        marca.put("comentario", comentario);
        return marca;
    }
}
