package com.usal.e3.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.Usuario;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RF-ADMIN-001 a 005 y 007: cuentas del personal, permisos por carrera, carreras, responsables y alertas. */
class AdminIntegrationTest extends IntegracionBase {

    private static final String ADMIN = "admin@usal.edu.ar";
    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";

    private Cookie admin;
    private Cookie aspirante;
    private int solicitud;
    private int carrera;

    /** Una solicitud en `DOC-REV`, sin responsables. */
    @BeforeEach
    void prepararSolicitud() throws Exception {
        admin(ADMIN);
        admin = loginAdmin(ADMIN);
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        carrera = jdbc.queryForObject("SELECT id FROM carreras WHERE codigo = 'ING-INF-001'", Integer.class);
    }

    @Test
    void elAdminCreaUnaCuentaYLeAsignaCarreras() throws Exception {
        Map<String, Object> alta = Map.of(
                "email", "l.diaz@usal.edu.ar",
                "nombre", "Laura",
                "apellido", "Díaz",
                "rolCodigo", "SECRE",
                "password", PASSWORD);
        postear("/admin/usuarios", conCampo(alta, "email", "l.diaz@gmail.com"), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
        postear("/admin/usuarios", conCampo(alta, "password", "1234"), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
        postear("/admin/usuarios", conCampo(alta, "rolCodigo", "ADMIN"), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rolCodigo").exists());
        int id = json.readTree(postear("/admin/usuarios", alta, admin)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.rol").value("SECRE"))
                        .andExpect(jsonPath("$.permisosCarrera", hasSize(0)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asInt();
        postear("/admin/usuarios", alta, admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICADO"));
        assertThat(filasAuditoria(ADMIN, "crear_cuenta")).isEqualTo(1);

        // La cuenta nueva entra con la contraseña que escribió el Admin, pero no al panel de administración.
        Cookie secretaria = loginPersonal("l.diaz@usal.edu.ar");
        mvc.perform(get("/admin/usuarios").cookie(secretaria)).andExpect(status().isForbidden());
        mvc.perform(get("/solicitudes/{id}", solicitud).cookie(secretaria))
                .andExpect(jsonPath("$.miRelacion").value("OBSERVADOR"));

        postear("/admin/usuarios/" + id + "/permisos/carreras", Map.of("carreraIds", List.of()), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.carreraIds").exists());
        postear("/admin/usuarios/" + id + "/permisos/carreras", Map.of("carreraIds", List.of(carrera)), admin)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permisosCarrera", hasSize(1)))
                .andExpect(jsonPath("$.permisosCarrera[0].carrera.id").value(carrera));
        verify(mail).enviarAviso(eq("l.diaz@usal.edu.ar"), conTexto("nuevo acceso"), conTexto("Se te ha asignado acceso"));
        mvc.perform(get("/solicitudes/{id}", solicitud).cookie(secretaria))
                .andExpect(jsonPath("$.miRelacion").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("TOMAR_PROCESO")));

        mvc.perform(get("/admin/usuarios").param("q", "díaz").cookie(admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("l.diaz@usal.edu.ar"));
        mvc.perform(get("/admin/usuarios").param("rol", "ACAD").cookie(admin))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void revocarConProcesoActivoPideForzarYDejaElProcesoSinResponsable() throws Exception {
        Usuario academico = usuario(ACADEMICO, "ACAD");
        PermisoCarrera permiso = permisoCarrera(academico, "ING-INF-001");
        Cookie sesion = loginPersonal(ACADEMICO);
        mvc.perform(post("/solicitudes/{id}/responsables/tomar", solicitud).cookie(sesion)).andExpect(status().isOk());

        String revocar = "/admin/permisos/carreras/" + permiso.getId() + "/revocar";
        postear(revocar, Map.of("forzar", false), admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROCESO_ACTIVO"))
                .andExpect(jsonPath("$.message", containsString("EXP-")));
        mvc.perform(get("/admin/usuarios/{id}", academico.getId()).cookie(admin))
                .andExpect(jsonPath("$.permisosCarrera", hasSize(1)));
        // Con el proceso tomado tampoco se puede desactivar la cuenta.
        enviar(patch("/admin/usuarios/" + academico.getId()), Map.of("activo", false), admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROCESO_ACTIVO"));

        postear(revocar, Map.of("forzar", true, "comentario", "Licencia"), admin).andExpect(status().isNoContent());
        mvc.perform(get("/solicitudes/{id}", solicitud).cookie(sesion))
                .andExpect(jsonPath("$.responsableAcademico").value(nullValue()))
                .andExpect(jsonPath("$.miRelacion").value("OBSERVADOR"));
        // No queda nadie con acceso a la carrera: el aviso es para el Admin y aparece en sus alertas.
        verify(mail).enviarAviso(eq(ADMIN), conTexto("sin responsable académico"), conTexto("Asigná a alguien"));
        mvc.perform(get("/admin/alertas").cookie(admin))
                .andExpect(jsonPath("$.sinResponsableAcademico[0].id").value(solicitud))
                .andExpect(jsonPath("$.sinResponsableAdministrativo[0].id").value(solicitud));
        mvc.perform(get("/solicitudes/{id}/historial", solicitud).cookie(admin))
                .andExpect(jsonPath("$[*].comentario", hasItem(containsString("Revocó el permiso de María Gómez"))));
        postear(revocar, Map.of("forzar", true), admin).andExpect(status().isNotFound());

        // Ya sin procesos, la cuenta se desactiva y deja de entrar.
        enviar(patch("/admin/usuarios/" + academico.getId()), Map.of("activo", false), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        postear("/auth/personal/login", Map.of("usuario", ACADEMICO, "password", PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdminAsignaUnResponsableConPermisoTemporal() throws Exception {
        Usuario academico = usuario(ACADEMICO, "ACAD");
        Usuario secretaria = usuario("c.ruiz@usal.edu.ar", "SECRE");
        String ruta = "/admin/solicitudes/" + solicitud + "/responsables";

        postear(ruta, Map.of("academicoId", academico.getId()), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
        postear(ruta, Map.of("comentario", "Reasignación"), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.academicoId").exists());
        postear(ruta, Map.of("academicoId", secretaria.getId(), "comentario", "Reasignación"), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.academicoId").exists());
        postear(ruta, Map.of("academicoId", academico.getId(), "comentario", "Reasignación"), admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SIN_PERMISO_CARRERA"));

        postear(
                        ruta,
                        Map.of("academicoId", academico.getId(), "crearPermisoTemporal", true, "comentario", "Reasignación"),
                        admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsableAcademico.id").value(academico.getId()));
        verify(mail).enviarAviso(eq(ACADEMICO), conTexto("te asignaron como responsable"), conTexto("Has sido asignado"));
        verify(mail).enviarAviso(eq(ASPIRANTE), conTexto("fue reasignado"), conTexto("María Gómez"));
        assertThat(filasAuditoria(ADMIN, "asignar_responsable")).isEqualTo(1);
        assertThat(filasAuditoria(ADMIN, "crear_permiso_temporal")).isEqualTo(1);

        // Con el permiso temporal es participante y responsable: ya puede revisar la documentación.
        Cookie sesion = loginPersonal(ACADEMICO);
        mvc.perform(get("/solicitudes/{id}", solicitud).cookie(sesion))
                .andExpect(jsonPath("$.miRelacion").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.accionesDisponibles", hasItem("ACEPTAR_DOCUMENTACION")));
        mvc.perform(get("/solicitudes/{id}/historial", solicitud).cookie(aspirante))
                .andExpect(jsonPath("$[*].comentario", hasItem(containsString("Asignó a María Gómez"))));
        int permiso = json.readTree(mvc.perform(get("/admin/usuarios/{id}", academico.getId()).cookie(admin))
                        .andExpect(jsonPath("$.permisosSolicitud", hasSize(1)))
                        .andExpect(jsonPath("$.permisosSolicitud[0].solicitud.id").value(solicitud))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .at("/permisosSolicitud/0/id")
                .asInt();

        // Al revocar el permiso temporal el proceso queda sin responsable.
        postear("/admin/permisos/solicitudes/" + permiso + "/revocar", Map.of("comentario", "Fin de la licencia"), admin)
                .andExpect(status().isNoContent());
        mvc.perform(get("/solicitudes/{id}", solicitud).cookie(sesion))
                .andExpect(jsonPath("$.responsableAcademico").value(nullValue()))
                .andExpect(jsonPath("$.miRelacion").value("OBSERVADOR"));
    }

    @Test
    void elAdminGestionaElCatalogoDeCarreras() throws Exception {
        Map<String, Object> nueva = Map.of(
                "codigo", "LIC-PSI-001",
                "nombre", "Licenciatura en Psicología",
                "facultad", "Facultad de Psicología",
                "descripcion", "Carrera de grado");
        postear("/carreras", nueva, aspirante).andExpect(status().isForbidden());
        postear("/carreras", conCampo(nueva, "nombre", " "), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
        int id = json.readTree(postear("/carreras", nueva, admin)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.activo").value(true))
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asInt();
        try {
            postear("/carreras", nueva, admin)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICADO"));
            enviar(put("/carreras/" + id), conCampo(nueva, "nombre", "Psicología"), admin)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Psicología"));
            enviar(put("/carreras/" + id), conCampo(nueva, "codigo", "ING-INF-001"), admin)
                    .andExpect(status().isConflict());

            enviar(patch("/carreras/" + id + "/activo"), Map.of("activo", false), admin)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activo").value(false));
            mvc.perform(get("/carreras").cookie(aspirante))
                    .andExpect(jsonPath("$[*].codigo", not(hasItem("LIC-PSI-001"))));
            mvc.perform(get("/carreras").param("activo", "todas").cookie(admin))
                    .andExpect(jsonPath("$[*].codigo", hasItem("LIC-PSI-001")));
            postear("/solicitudes", datosSolicitud("LIC-PSI-001"), aspirante).andExpect(status().isBadRequest());
            assertThat(filasAuditoria(ADMIN, "desactivar_carrera")).isEqualTo(1);
        } finally {
            // `carreras` es un catálogo: la limpieza entre tests no lo toca.
            jdbc.update("DELETE FROM carreras WHERE id = ?", id);
        }
    }

    private static Map<String, Object> conCampo(Map<String, Object> base, String campo, Object valor) {
        Map<String, Object> copia = new java.util.HashMap<>(base);
        copia.put(campo, valor);
        return copia;
    }
}
