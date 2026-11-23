package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/** Campana de notificaciones (contrato §8.2), auditoría general (RF-AUDIT-003) y exportación (RF-PROC-005). */
class CampanaAuditoriaExpedienteIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";

    private Cookie aspirante;
    private Cookie academico;
    private int solicitud;

    /** El Académico ya tiene permiso cuando nace la solicitud: recibe los avisos. Queda en `DOC-REV`. */
    @BeforeEach
    void prepararSolicitud() throws Exception {
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
    }

    @Test
    void laCampanaReplicaLosAvisosDeCadaUno() throws Exception {
        // "Nueva solicitud" y "documentación pendiente de revisión": los dos mails del Académico.
        mvc.perform(get("/notificaciones/no-leidas/cantidad").cookie(academico))
                .andExpect(jsonPath("$.cantidad").value(2));
        mvc.perform(get("/notificaciones/no-leidas/cantidad").cookie(aspirante))
                .andExpect(jsonPath("$.cantidad").value(0));
        MvcResult lista = mvc.perform(get("/notificaciones").cookie(academico))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].titulo", containsString("documentación pendiente de revisión")))
                .andExpect(jsonPath("$.content[0].solicitudId").value(solicitud))
                .andExpect(jsonPath("$.content[0].leida").value(false))
                .andReturn();
        int id = json.readTree(lista.getResponse().getContentAsString()).at("/content/0/id").asInt();

        // Cada uno marca solo las suyas.
        mvc.perform(patch("/notificaciones/{id}", id).cookie(aspirante)).andExpect(status().isNotFound());
        mvc.perform(patch("/notificaciones/{id}", id).cookie(academico)).andExpect(status().isNoContent());
        mvc.perform(get("/notificaciones").param("leida", "false").cookie(academico))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(post("/notificaciones/marcar-todas-leidas").cookie(academico)).andExpect(status().isNoContent());
        mvc.perform(get("/notificaciones/no-leidas/cantidad").cookie(academico))
                .andExpect(jsonPath("$.cantidad").value(0));
    }

    @Test
    void laAuditoriaLaLeenElAdminYElRevisor() throws Exception {
        usuario("r.sosa@usal.edu.ar", "REVISOR");
        Cookie revisor = loginPersonal("r.sosa@usal.edu.ar");
        mvc.perform(get("/auditoria").cookie(academico)).andExpect(status().isForbidden());
        mvc.perform(get("/auditoria").cookie(aspirante)).andExpect(status().isForbidden());
        mvc.perform(get("/solicitudes/{id}/auditoria", solicitud).cookie(academico)).andExpect(status().isForbidden());

        MvcResult cambios = mvc.perform(get("/auditoria").param("accion", "cambio_estado").cookie(revisor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].accion", everyItem(is("cambio_estado"))))
                // Lo más nuevo primero, con los estados por código.
                .andExpect(jsonPath("$.content[0].estadoCodigoAnterior").value("SOL-INI"))
                .andExpect(jsonPath("$.content[0].estadoCodigoNuevo").value("DOC-REV"))
                .andExpect(jsonPath("$.content[0].usuario.email").value(ASPIRANTE))
                .andReturn();
        int id = json.readTree(cambios.getResponse().getContentAsString()).at("/content/0/id").asInt();
        mvc.perform(get("/auditoria/{id}", id).cookie(revisor))
                .andExpect(jsonPath("$.entidad").value("solicitud"))
                .andExpect(jsonPath("$.entidadId").value(solicitud));

        mvc.perform(get("/auditoria").param("usuario", "GOMEZ").cookie(revisor))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].usuario.email", everyItem(is(ACADEMICO))));
        mvc.perform(get("/auditoria").param("hasta", "2020-01-01").cookie(revisor))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/auditoria/opciones").cookie(revisor))
                .andExpect(jsonPath("$.acciones", hasItem("cambio_estado")))
                .andExpect(jsonPath("$.entidades", hasItem("solicitud")));
        mvc.perform(get("/solicitudes/{id}/auditoria", solicitud).cookie(revisor))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content[*].entidadId", everyItem(is(solicitud))));

        // El Revisor lee, pero no administra.
        mvc.perform(get("/admin/alertas").cookie(revisor)).andExpect(status().isForbidden());
    }

    @Test
    void elPersonalDescargaElExpedienteEnZip() throws Exception {
        mvc.perform(get("/solicitudes/{id}/expediente", solicitud).cookie(aspirante)).andExpect(status().isForbidden());
        mvc.perform(get("/solicitudes/expedientes").cookie(aspirante)).andExpect(status().isForbidden());

        MvcResult individual = mvc.perform(get("/solicitudes/{id}/expediente", solicitud).cookie(academico))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition", containsString("Expediente_EXP-")))
                .andReturn();
        assertThat(entradas(individual))
                .contains("resumen.pdf", "documentos/01-analitico.pdf", "documentos/02-plan.pdf", "documentos/03-programa.pdf");
        assertThat(filasAuditoria(ACADEMICO, "descarga_expediente")).isEqualTo(1);

        MvcResult multiple = mvc.perform(get("/solicitudes/expedientes").param("estado", "DOC-REV").cookie(academico))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Expedientes_")))
                .andReturn();
        assertThat(entradas(multiple)).hasSize(4).allMatch(nombre -> nombre.startsWith("EXP-"));
        assertThat(filasAuditoria(ACADEMICO, "descarga_expedientes")).isEqualTo(1);

        mvc.perform(get("/solicitudes/expedientes").param("estado", "E3-INI").cookie(academico))
                .andExpect(status().isNotFound());
    }

    private static List<String> entradas(MvcResult respuesta) throws Exception {
        List<String> nombres = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(respuesta.getResponse().getContentAsByteArray()))) {
            for (ZipEntry entrada = zip.getNextEntry(); entrada != null; entrada = zip.getNextEntry()) {
                nombres.add(entrada.getName());
            }
        }
        return nombres;
    }
}
