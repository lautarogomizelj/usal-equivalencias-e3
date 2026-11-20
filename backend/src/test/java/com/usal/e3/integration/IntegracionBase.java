package com.usal.e3.integration;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.RolRepository;
import com.usal.e3.repository.UsuarioRepository;
import com.usal.e3.security.CookieSesion;
import com.usal.e3.service.MailService;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Corre contra un PostgreSQL real con las migraciones de Flyway (`make backend-test`). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(IntegracionBase.CsrfEnLosPedidos.class)
abstract class IntegracionBase {

    /**
     * Todos los pedidos de los tests llevan el token CSRF como lo manda el frontend: cookie y header iguales.
     * No se usa `csrf()` de spring-security-test porque reemplaza el repositorio de tokens del contexto.
     */
    @TestConfiguration
    static class CsrfEnLosPedidos {

        @Bean
        MockMvcBuilderCustomizer conCsrf() {
            return builder -> builder.defaultRequest(
                    get("/").cookie(new Cookie("XSRF-TOKEN", "token-de-test")).header("X-XSRF-TOKEN", "token-de-test"));
        }
    }

    protected static final String PASSWORD = "Clave#2026";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected AspiranteRepository aspirantes;

    @Autowired
    protected UsuarioRepository usuarios;

    @Autowired
    private RolRepository roles;

    @Autowired
    private CarreraRepository carreras;

    @Autowired
    private PermisoCarreraRepository permisos;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    protected MailService mail;

    @BeforeEach
    void limpiarBase() {
        jdbc.execute("""
                TRUNCATE sesiones, codigos_verificacion, user_career_permissions, auditoria_sistema,
                         user_solicitud_permissions, documentos_solicitud, estados_solicitud, solicitudes,
                         aspirantes, usuarios RESTART IDENTITY CASCADE
                """);
    }

    protected Map<String, Object> datosSolicitud(String carreraDestinoCodigo) {
        return Map.of(
                "universidadOrigen", "Universidad Nacional de La Plata",
                "facultadOrigen", "Facultad de Informática",
                "carreraOrigen", "Licenciatura en Sistemas",
                "carreraDestinoCodigo", carreraDestinoCodigo,
                "condicionAcademica", "REGULAR");
    }

    /** Crea una solicitud en `SOL-INI` y devuelve su id. */
    protected int crearSolicitud(Cookie sesion) throws Exception {
        MvcResult resultado = postear("/solicitudes", datosSolicitud("ING-INF-001"), sesion)
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("id").asInt();
    }

    protected static MockMultipartFile pdf(String nombre, int tamano) {
        byte[] contenido = new byte[tamano];
        System.arraycopy("%PDF-1.7".getBytes(), 0, contenido, 0, 8);
        return new MockMultipartFile("archivo", nombre, "application/pdf", contenido);
    }

    protected ResultActions subir(Cookie sesion, int solicitudId, String tipo, MockMultipartFile archivo)
            throws Exception {
        return mvc.perform(multipart("/solicitudes/{id}/documentos", solicitudId)
                .file(archivo)
                .param("tipo", tipo)
                .cookie(sesion));
    }

    /** Sube Analítico, Plan y un Programa, y envía la documentación: la solicitud queda en `DOC-REV`. */
    protected void enviarDocumentacion(Cookie sesion, int solicitudId) throws Exception {
        for (String tipo : new String[] {"ANALITICO", "PLAN", "PROGRAMA"}) {
            subir(sesion, solicitudId, tipo, pdf(tipo.toLowerCase() + ".pdf", 1024)).andExpect(status().isCreated());
        }
        postear("/solicitudes/" + solicitudId + "/documentacion/enviar", Map.of("estadoEsperado", "SOL-INI"), sesion)
                .andExpect(status().isOk());
    }

    protected ResultActions enviar(MockHttpServletRequestBuilder pedido, Object cuerpo, Cookie... cookies)
            throws Exception {
        pedido.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
        return mvc.perform(cookies.length == 0 ? pedido : pedido.cookie(cookies));
    }

    protected ResultActions postear(String ruta, Object cuerpo, Cookie... cookies) throws Exception {
        return enviar(post(ruta), cuerpo, cookies);
    }

    protected Map<String, Object> datosRegistro(String email, String dni) {
        return Map.of(
                "email", email,
                "password", PASSWORD,
                "nombre", "Juan",
                "apellido", "Pérez",
                "telefono", "+54 11 5555-0000",
                "dni", dni);
    }

    /** Registra sin validar y devuelve el código que salió por mail. */
    protected String registrar(String email, String dni) throws Exception {
        postear("/auth/registro", datosRegistro(email, dni)).andExpect(status().isCreated());
        return ultimoCodigoValidacion(email);
    }

    protected String ultimoCodigoValidacion(String email) {
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(mail, atLeastOnce()).enviarCodigoValidacion(eq(email), codigo.capture());
        return codigo.getValue();
    }

    protected String ultimoCodigoRecupero(String email) {
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(mail, atLeastOnce()).enviarCodigoRecupero(eq(email), codigo.capture());
        return codigo.getValue();
    }

    /** Aspirante ya validado, sin sesión. */
    protected Aspirante aspiranteValidado(String email, String dni) {
        Aspirante aspirante =
                new Aspirante(email, passwordEncoder.encode(PASSWORD), "Juan", "Pérez", "+54 11 5555-0000", dni);
        aspirante.setEmailValidado(true);
        return aspirantes.save(aspirante);
    }

    protected Cookie loginAspirante(String email) throws Exception {
        return cookie(postear("/auth/login", Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isOk()));
    }

    protected Usuario usuario(String email, String rol) {
        Usuario usuario = new Usuario(email, "María", "Gómez", roles.findByCodigo(rol).orElseThrow());
        usuario.setPasswordHash(passwordEncoder.encode(PASSWORD));
        return usuarios.save(usuario);
    }

    protected Usuario admin(String email) {
        Usuario admin = new Usuario(email, "Admin", "General", roles.findByCodigo("ADMIN").orElseThrow());
        admin.setPasswordHash(passwordEncoder.encode(PASSWORD));
        return usuarios.save(admin);
    }

    protected Cookie loginAdmin(String email) throws Exception {
        return cookie(postear("/auth/admin/login", Map.of("usuario", email, "password", PASSWORD))
                .andExpect(status().isOk()));
    }

    protected Cookie loginPersonal(String email) throws Exception {
        return cookie(postear("/auth/personal/login", Map.of("usuario", email, "password", PASSWORD))
                .andExpect(status().isOk()));
    }

    protected PermisoCarrera permisoCarrera(Usuario usuario, String codigoCarrera) {
        return permisos.save(new PermisoCarrera(
                usuario.getId(), carreras.findByCodigo(codigoCarrera).orElseThrow()));
    }

    protected PermisoCarrera guardar(PermisoCarrera permiso) {
        return permisos.save(permiso);
    }

    protected Cookie cookie(ResultActions resultado) {
        return resultado.andReturn().getResponse().getCookie(CookieSesion.NOMBRE);
    }

    protected int filasAuditoria(String email, String accion) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria_sistema WHERE usuario_email = ? AND accion = ?",
                Integer.class,
                email,
                accion);
    }
}
