package com.usal.e3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

/** RF-MSG-001 a 004: chat de la solicitud entre el aspirante y los responsables (contrato §8.1). */
class ChatIntegrationTest extends IntegracionBase {

    private static final String ASPIRANTE = "juan.perez@gmail.com";
    private static final String ACADEMICO = "m.gomez@usal.edu.ar";
    private static final String OTRO_ACADEMICO = "p.sosa@usal.edu.ar";

    private Cookie aspirante;
    private Cookie academico;
    private int solicitud;

    @BeforeEach
    void dejarLaSolicitudEnviadaYSinResponsable() throws Exception {
        aspiranteValidado(ASPIRANTE, "40123456");
        aspirante = loginAspirante(ASPIRANTE);
        solicitud = crearSolicitud(aspirante);
        enviarDocumentacion(aspirante, solicitud);
        permisoCarrera(usuario(ACADEMICO, "ACAD"), "ING-INF-001");
        academico = loginPersonal(ACADEMICO);
    }

    @Test
    void sinResponsableElChatEstaDeshabilitado() throws Exception {
        mvc.perform(get(ruta("")).cookie(aspirante))
                .andExpect(jsonPath("$.chatHabilitado").value(false))
                .andExpect(jsonPath("$.puedeEscribirChat").value(false))
                .andExpect(jsonPath("$.mensajesNoLeidos").value(0));
        mensaje(aspirante, "Hola", "ESTADO", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CHAT_DESHABILITADO"));
        // Participa de la carrera, pero todavía no tomó el proceso.
        mensaje(academico, "Hola", "ESTADO", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));
        mvc.perform(get(ruta("/chat/mensajes")).cookie(aspirante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()));
    }

    @Test
    void elAspiranteYElResponsableConversan() throws Exception {
        tomar(academico);
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.puedeEscribirChat").value(true));
        mvc.perform(get(ruta("")).cookie(academico)).andExpect(jsonPath("$.puedeEscribirChat").value(true));

        mensaje(aspirante, "  ¿Falta algún documento?  ", "DOCUMENTACION", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.autor.tipo").value("ASPIRANTE"))
                .andExpect(jsonPath("$.autor.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.autor.rol").value("ASPIRANTE"))
                .andExpect(jsonPath("$.contenido").value("¿Falta algún documento?"))
                .andExpect(jsonPath("$.tipoConsulta").value("DOCUMENTACION"))
                .andExpect(jsonPath("$.adjunto").doesNotExist())
                .andExpect(jsonPath("$.esMio").value(true))
                .andExpect(jsonPath("$.visto").value(false));

        // Los propios no cuentan como pendientes; los del otro, hasta que los marca como leídos.
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.mensajesNoLeidos").value(0));
        mvc.perform(get(ruta("")).cookie(academico)).andExpect(jsonPath("$.mensajesNoLeidos").value(1));
        mvc.perform(get(ruta("/chat/mensajes")).cookie(academico))
                .andExpect(jsonPath("$.content[0].esMio").value(false))
                .andExpect(jsonPath("$.content[0].visto").value(false));
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 1), academico).andExpect(status().isNoContent());
        mvc.perform(get(ruta("")).cookie(academico)).andExpect(jsonPath("$.mensajesNoLeidos").value(0));
        mvc.perform(get(ruta("/chat/mensajes")).cookie(aspirante))
                .andExpect(jsonPath("$.content[0].visto").value(true));

        mensaje(academico, null, "DOCUMENTACION", pdf("modelo.pdf", 2048))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.tipo").value("USUARIO"))
                .andExpect(jsonPath("$.autor.nombre").value("María Gómez"))
                .andExpect(jsonPath("$.autor.rol").value("ACAD"))
                .andExpect(jsonPath("$.contenido").value(""))
                .andExpect(jsonPath("$.adjunto.nombre").value("modelo.pdf"))
                .andExpect(jsonPath("$.adjunto.urlDescarga")
                        .value("/solicitudes/" + solicitud + "/chat/mensajes/2/adjunto"));
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.mensajesNoLeidos").value(1));

        // `despuesDe` trae solo lo nuevo, que es lo que pide el frontend mientras el chat está abierto.
        mvc.perform(get(ruta("/chat/mensajes?despuesDe=1")).cookie(aspirante))
                .andExpect(jsonPath("$.content[*].id", contains(2)))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(ruta("/chat/mensajes?size=1")).cookie(aspirante))
                .andExpect(jsonPath("$.content[*].id", contains(1)))
                .andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get(ruta("/chat/mensajes/2/adjunto")).cookie(aspirante)).andExpect(status().isOk());
        mvc.perform(get(ruta("/chat/mensajes/1/adjunto")).cookie(aspirante)).andExpect(status().isNotFound());

        // Marcar como leído un mensaje anterior no desmarca los que siguen.
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 2), aspirante).andExpect(status().isNoContent());
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 1), aspirante).andExpect(status().isNoContent());
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.mensajesNoLeidos").value(0));

        aspiranteValidado("otra@gmail.com", "40999888");
        Cookie ajeno = loginAspirante("otra@gmail.com");
        mvc.perform(get(ruta("/chat/mensajes")).cookie(ajeno)).andExpect(status().isNotFound());
        mvc.perform(get(ruta("/chat/mensajes/2/adjunto")).cookie(ajeno)).andExpect(status().isNotFound());
        mensaje(ajeno, "Hola", "OTRO", null).andExpect(status().isNotFound());
    }

    @Test
    void elMensajeSeValida() throws Exception {
        tomar(academico);

        mensaje(aspirante, "  ", "OTRO", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contenido").exists());
        mensaje(aspirante, "x".repeat(501), "OTRO", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contenido").exists());
        mensaje(aspirante, "Hola", "SALUDO", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipoConsulta").exists());
        mensaje(aspirante, "Hola", null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipoConsulta").exists());
        mensaje(aspirante, "Va el archivo", "OTRO", adjunto("grande.pdf", "%PDF-1.7", 5 * 1024 * 1024 + 1))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("LIMITE_TAMANO"));
        mensaje(aspirante, "Va el archivo", "OTRO", adjunto("planilla.xlsx", "PK", 1024))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ARCHIVO_INVALIDO"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mensajes_chat", Integer.class)).isZero();

        // Además de PDF y JPG, el chat acepta PNG (interfaces, Anexo A #16), y un mensaje de 500 caracteres justos.
        mensaje(aspirante, "x".repeat(500), "OTRO", adjunto("captura.png", "\u0089PNG", 1024))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adjunto.nombre").value("captura.png"));
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 99), academico)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.hastaMensajeId").exists());
    }

    @Test
    void elRestoDelPersonalSoloLee() throws Exception {
        tomar(academico);
        mensaje(aspirante, "Hola", "ESTADO", null).andExpect(status().isCreated());

        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");
        Cookie participante = loginPersonal(OTRO_ACADEMICO);
        usuario("observador@usal.edu.ar", "ACAD");
        Cookie observador = loginPersonal("observador@usal.edu.ar");
        usuario("revisor@usal.edu.ar", "REVISOR");
        Cookie revisor = loginPersonal("revisor@usal.edu.ar");
        admin("admin@usal.edu.ar");
        Cookie administrador = loginAdmin("admin@usal.edu.ar");

        for (Cookie lector : new Cookie[] {participante, observador, revisor, administrador}) {
            mvc.perform(get(ruta("/chat/mensajes")).cookie(lector))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
            mvc.perform(get(ruta("")).cookie(lector))
                    .andExpect(jsonPath("$.puedeEscribirChat").value(false))
                    .andExpect(jsonPath("$.mensajesNoLeidos").value(0));
        }
        mensaje(participante, "Hola", "ESTADO", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));
        mensaje(observador, "Hola", "ESTADO", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_PARTICIPANTE"));
        mensaje(revisor, "Hola", "ESTADO", null).andExpect(status().isForbidden());
        mensaje(administrador, "Hola", "ESTADO", null).andExpect(status().isForbidden());

        // Que lo abra alguien que no participa del chat no lo marca como visto.
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 1), observador).andExpect(status().isForbidden());
        postear(ruta("/chat/leidos"), Map.of("hastaMensajeId", 1), participante).andExpect(status().isForbidden());
        mvc.perform(get(ruta("/chat/mensajes")).cookie(aspirante))
                .andExpect(jsonPath("$.content[0].visto").value(false));
    }

    @Test
    void elChatEsDeLaSolicitudYSigueAlCambiarElResponsable() throws Exception {
        tomar(academico);
        mensaje(academico, "Estoy revisando tu documentación", "ESTADO", null).andExpect(status().isCreated());

        // Secretaría también escribe cuando es la responsable administrativa.
        permisoCarrera(usuario("secretaria@usal.edu.ar", "SECRE"), "ING-INF-001");
        Cookie secretaria = loginPersonal("secretaria@usal.edu.ar");
        tomar(secretaria);
        mensaje(secretaria, "El cupón llega al final del trámite", "CALCULOS", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.rol").value("SECRE"));

        permisoCarrera(usuario(OTRO_ACADEMICO, "ACAD"), "ING-INF-001");
        Cookie reemplazo = loginPersonal(OTRO_ACADEMICO);
        mvc.perform(post(ruta("/responsables/baja")).cookie(academico)).andExpect(status().isOk());
        mensaje(academico, "Sigo acá", "OTRO", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_RESPONSABLE"));
        tomar(reemplazo);

        // El nuevo responsable ve el historial completo, con quién escribió cada mensaje.
        mvc.perform(get(ruta("/chat/mensajes")).cookie(reemplazo))
                .andExpect(jsonPath("$.content[*].autor.id", contains(1, 2)))
                .andExpect(jsonPath("$.content[*].esMio", contains(false, false)));
        mvc.perform(get(ruta("")).cookie(reemplazo)).andExpect(jsonPath("$.mensajesNoLeidos").value(2));
        mensaje(reemplazo, "Tomo el proceso desde hoy", "ESTADO", null).andExpect(status().isCreated());
        mvc.perform(get(ruta("")).cookie(aspirante)).andExpect(jsonPath("$.mensajesNoLeidos").value(3));
    }

    private String ruta(String sufijo) {
        return "/solicitudes/" + solicitud + sufijo;
    }

    private void tomar(Cookie sesion) throws Exception {
        mvc.perform(post(ruta("/responsables/tomar")).cookie(sesion)).andExpect(status().isOk());
    }

    private static MockMultipartFile adjunto(String nombre, String firma, int tamano) {
        byte[] contenido = new byte[tamano];
        for (int i = 0; i < firma.length(); i++) {
            contenido[i] = (byte) firma.charAt(i);
        }
        return new MockMultipartFile("adjunto", nombre, "application/octet-stream", contenido);
    }

    private ResultActions mensaje(Cookie sesion, String contenido, String tipoConsulta, MockMultipartFile archivo)
            throws Exception {
        MockMultipartHttpServletRequestBuilder pedido = multipart(ruta("/chat/mensajes"));
        if (archivo != null) {
            pedido.file(new MockMultipartFile(
                    "adjunto", archivo.getOriginalFilename(), archivo.getContentType(), archivo.getBytes()));
        }
        if (contenido != null) {
            pedido.param("contenido", contenido);
        }
        if (tipoConsulta != null) {
            pedido.param("tipoConsulta", tipoConsulta);
        }
        return mvc.perform(pedido.cookie(sesion));
    }
}
