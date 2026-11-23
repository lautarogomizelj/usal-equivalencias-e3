package com.usal.e3.service;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.PermisoSolicitud;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.PermisoSolicitudRepository;
import com.usal.e3.repository.RolRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.repository.UsuarioRepository;
import com.usal.e3.security.SesionActual;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo de Administración (RF-ADMIN-001 a 005 y 007, contrato §9): cuentas del personal, permisos por carrera,
 * carreras, responsables y alertas. Todo queda en auditoría a nombre del Admin.
 */
@Service
public class AdminService {

    private static final List<String> ROLES_DEL_PERSONAL = List.of(Rol.ACADEMICO, Rol.SECRETARIA, Rol.REVISOR);

    /** Solo los permisos activos: sin revocar y, los temporales, sin vencer. */
    public record DatosUsuario(
            Usuario usuario, List<PermisoCarrera> permisosCarrera, List<PermisoTemporal> permisosSolicitud) {}

    public record PermisoTemporal(PermisoSolicitud permiso, Solicitud solicitud) {}

    public record Alertas(
            List<Solicitud> sinResponsableAcademico,
            List<Solicitud> sinResponsableAdministrativo,
            List<Cuenta> cuentasBloqueadas) {}

    private final UsuarioRepository usuarios;
    private final AspiranteRepository aspirantes;
    private final RolRepository roles;
    private final CarreraRepository carreras;
    private final SolicitudRepository solicitudes;
    private final PermisoCarreraRepository permisosCarrera;
    private final PermisoSolicitudRepository permisosSolicitud;
    private final PermisosService permisos;
    private final CatalogoService catalogo;
    private final CuentaService cuentas;
    private final AuditoriaService auditoria;
    private final NotificacionService notificaciones;
    private final SolicitudService detalles;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UsuarioRepository usuarios,
            AspiranteRepository aspirantes,
            RolRepository roles,
            CarreraRepository carreras,
            SolicitudRepository solicitudes,
            PermisoCarreraRepository permisosCarrera,
            PermisoSolicitudRepository permisosSolicitud,
            PermisosService permisos,
            CatalogoService catalogo,
            CuentaService cuentas,
            AuditoriaService auditoria,
            NotificacionService notificaciones,
            SolicitudService detalles,
            PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.aspirantes = aspirantes;
        this.roles = roles;
        this.carreras = carreras;
        this.solicitudes = solicitudes;
        this.permisosCarrera = permisosCarrera;
        this.permisosSolicitud = permisosSolicitud;
        this.permisos = permisos;
        this.catalogo = catalogo;
        this.cuentas = cuentas;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
        this.detalles = detalles;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Cuentas del personal (RF-ADMIN-001) ---

    /** El personal @usal, por apellido. El Admin no aparece: su cuenta no se gestiona desde acá. */
    @Transactional(readOnly = true)
    public Page<DatosUsuario> usuarios(String rol, Boolean activo, String q, Pageable pagina) {
        String busqueda = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        List<Usuario> todos = usuarios.findAll(Sort.by("apellido", "nombre")).stream()
                .filter(u -> !Rol.ADMIN.equals(u.rolCodigo()))
                .filter(u -> rol == null || rol.isBlank() || rol.equals(u.rolCodigo()))
                .filter(u -> activo == null || u.isActivo() == activo)
                .filter(u -> busqueda.isEmpty()
                        || u.nombreCompleto().toLowerCase(Locale.ROOT).contains(busqueda)
                        || u.getEmail().contains(busqueda))
                .toList();
        int desde = (int) Math.min(pagina.getOffset(), todos.size());
        int hasta = Math.min(desde + pagina.getPageSize(), todos.size());
        return new PageImpl<>(todos.subList(desde, hasta).stream().map(this::datos).toList(), pagina, todos.size());
    }

    @Transactional(readOnly = true)
    public DatosUsuario usuario(Integer id) {
        return datos(personal(id));
    }

    /**
     * La cuenta nace con acceso básico, sin carreras. Mientras el personal entre con usuario y contraseña
     * (CC-016), la contraseña inicial la escribe el Admin (CC-024).
     */
    @Transactional
    public DatosUsuario crear(
            SesionActual actual, String email, String nombre, String apellido, String rolCodigo, String password, String ip) {
        String normalizado = Validaciones.normalizarEmail(email);
        if (!normalizado.endsWith("@" + AuthService.DOMINIO_INSTITUCIONAL)) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "email", "El email tiene que ser @usal.edu.ar.");
        }
        Rol rol = rolDelPersonal(rolCodigo);
        exigirPassword(password);
        if (usuarios.existsByEmail(normalizado)) {
            throw new ApiException(ErrorCode.DUPLICADO, "Ya existe una cuenta con ese email.");
        }
        Usuario usuario = new Usuario(normalizado, nombre.strip(), apellido.strip(), rol);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuarios.save(usuario);
        auditoria.registrar(cuentas.de(actual), "crear_cuenta", usuario, Map.of("rol", rolCodigo), ip);
        return datos(usuario);
    }

    /**
     * Cada dato es opcional. Con procesos activos no se puede cambiar el rol ni desactivar la cuenta: primero
     * hay que reasignar esas solicitudes.
     */
    @Transactional
    public DatosUsuario editar(
            SesionActual actual,
            Integer id,
            String nombre,
            String apellido,
            String rolCodigo,
            Boolean activo,
            String password,
            String ip) {
        Usuario usuario = personal(id);
        Map<String, Object> cambios = new HashMap<>();
        boolean cambiaRol = rolCodigo != null && !rolCodigo.equals(usuario.rolCodigo());
        boolean desactiva = Boolean.FALSE.equals(activo) && usuario.isActivo();
        if (cambiaRol || desactiva) {
            exigirSinProcesos(procesosActivos(usuario));
        }
        if (nombre != null && !nombre.isBlank()) {
            usuario.setNombre(nombre.strip());
        }
        if (apellido != null && !apellido.isBlank()) {
            usuario.setApellido(apellido.strip());
        }
        if (cambiaRol) {
            usuario.setRol(rolDelPersonal(rolCodigo));
            cambios.put("rol", rolCodigo);
        }
        if (activo != null && activo != usuario.isActivo()) {
            usuario.setActivo(activo);
            cambios.put("activo", activo);
        }
        if (password != null && !password.isEmpty()) {
            exigirPassword(password);
            usuario.setPasswordHash(passwordEncoder.encode(password));
            cambios.put("password", "cambiada");
        }
        auditoria.registrar(cuentas.de(actual), "editar_cuenta", usuario, cambios, ip);
        return datos(usuario);
    }

    // --- Permisos (RF-ADMIN-003 y 004) ---

    /** Un permiso por carrera; las que ya tiene se saltean. Avisa al usuario por cada carrera nueva. */
    @Transactional
    public DatosUsuario asignarCarreras(SesionActual actual, Integer id, List<Integer> carreraIds, String ip) {
        Usuario usuario = personal(id);
        if (Rol.REVISOR.equals(usuario.rolCodigo())) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "carreraIds", "Solo Académico y Secretaría trabajan por carrera.");
        }
        if (carreraIds == null || carreraIds.isEmpty()) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "carreraIds", "Elegí al menos una carrera.");
        }
        Cuenta admin = cuentas.de(actual);
        for (Integer carreraId : carreraIds.stream().distinct().toList()) {
            Carrera carrera = carreras.findById(carreraId)
                    .filter(Carrera::isActivo)
                    .orElseThrow(() -> ApiException.enCampo(
                            ErrorCode.VALIDACION, "carreraIds", "Elegí carreras activas del catálogo."));
            PermisoCarrera permiso = permisosCarrera.findByUserIdAndCarrera_Id(id, carreraId).orElse(null);
            if (permiso != null && permiso.getRevokedAt() == null) {
                continue;
            }
            if (permiso == null) {
                permisosCarrera.save(new PermisoCarrera(id, carrera));
            } else {
                permiso.reactivar();
            }
            auditoria.registrar(admin, "asignar_permiso", usuario, Map.of("carrera", carrera.getCodigo()), ip);
            notificaciones.permisoAsignado(usuario, carrera);
        }
        return datos(usuario);
    }

    /**
     * Con procesos activos en la carrera responde 409 `PROCESO_ACTIVO`, salvo que venga `forzar`: entonces el
     * usuario pierde el acceso enseguida y esos procesos quedan sin responsable.
     */
    @Transactional
    public void revocarCarrera(SesionActual actual, Integer permisoId, boolean forzar, String comentario, String ip) {
        PermisoCarrera permiso = permisosCarrera.findById(permisoId)
                .filter(p -> p.getRevokedAt() == null)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Usuario usuario = usuarios.findById(permiso.getUserId()).orElseThrow();
        Cuenta admin = cuentas.de(actual);
        List<Solicitud> candidatas = procesosActivos(usuario).stream()
                .filter(s -> s.getCarreraDestino().getId().equals(permiso.getCarrera().getId()))
                .toList();
        permiso.setRevokedAt(OffsetDateTime.now());
        permisosCarrera.flush();
        // Con un permiso temporal sobre la solicitud sigue siendo participante: ese proceso no se toca.
        List<Solicitud> afectadas =
                candidatas.stream().filter(s -> !permisos.esParticipante(usuario, s)).toList();
        if (!forzar) {
            exigirSinProcesos(afectadas);
        }
        auditoria.registrarEn(
                admin,
                "revocar_permiso",
                "usuario",
                usuario.getId(),
                Map.of("carrera", permiso.getCarrera().getCodigo(), "forzado", !afectadas.isEmpty()),
                limpio(comentario),
                ip);
        afectadas.forEach(solicitud -> quitarResponsable(solicitud, usuario, admin, ip));
    }

    /** El permiso temporal no pide doble confirmación (contrato §9): si era el responsable, el proceso se libera. */
    @Transactional
    public void revocarSolicitud(SesionActual actual, Integer permisoId, String comentario, String ip) {
        PermisoSolicitud permiso = permisosSolicitud.findById(permisoId)
                .filter(p -> permisosSolicitud.activo(p.getUserId(), p.getSolicitudId(), OffsetDateTime.now()))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Usuario usuario = usuarios.findById(permiso.getUserId()).orElseThrow();
        Solicitud solicitud = solicitudes.findById(permiso.getSolicitudId()).orElseThrow();
        Cuenta admin = cuentas.de(actual);
        permiso.setRevokedAt(OffsetDateTime.now());
        permisosSolicitud.flush();
        auditoria.registrarEn(
                admin,
                "revocar_permiso",
                "usuario",
                usuario.getId(),
                Map.of("solicitud", solicitud.getCodigoExp()),
                limpio(comentario),
                ip);
        if (solicitud.tieneComoResponsable(usuario) && !permisos.esParticipante(usuario, solicitud)) {
            quitarResponsable(solicitud, usuario, admin, ip);
        }
    }

    // --- Responsables (RF-ADMIN-005) ---

    /**
     * Asigna o reemplaza al responsable académico, al administrativo o a los dos. A quien no tiene permiso sobre
     * la carrera se le crea uno temporal sobre la solicitud, si el Admin lo pidió; si no, 409 `SIN_PERMISO_CARRERA`.
     */
    @Transactional
    public DetalleSolicitud asignarResponsables(
            SesionActual actual,
            Integer solicitudId,
            Integer academicoId,
            Integer administrativoId,
            boolean crearPermisoTemporal,
            OffsetDateTime expiresAt,
            String comentario,
            String ip) {
        String motivo = limpio(comentario);
        if (motivo == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "comentario", "Escribí el motivo de la asignación.");
        }
        if (academicoId == null && administrativoId == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "academicoId", "Elegí al menos un responsable.");
        }
        if (expiresAt != null && !expiresAt.isAfter(OffsetDateTime.now())) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "expiresAt", "El vencimiento tiene que ser futuro.");
        }
        Usuario admin = (Usuario) cuentas.de(actual);
        Solicitud solicitud = solicitudes.buscarParaActualizar(solicitudId)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        if (catalogo.estadosPorCodigo().get(solicitud.getEstadoCodigo()).isEsTerminal()) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        Map<String, Integer> elegidos = new HashMap<>();
        elegidos.put(Rol.ACADEMICO, academicoId);
        elegidos.put(Rol.SECRETARIA, administrativoId);
        for (String rol : List.of(Rol.ACADEMICO, Rol.SECRETARIA)) {
            Integer elegido = elegidos.get(rol);
            Usuario anterior = solicitud.responsableDe(rol);
            if (elegido == null || (anterior != null && anterior.getId().equals(elegido))) {
                continue;
            }
            String campo = Rol.ACADEMICO.equals(rol) ? "academicoId" : "administrativoId";
            Usuario nuevo = usuarios.findById(elegido)
                    .filter(u -> u.isActivo() && rol.equals(u.rolCodigo()))
                    .orElseThrow(() -> ApiException.enCampo(
                            ErrorCode.VALIDACION, campo, "Elegí una cuenta activa de ese rol."));
            if (!permisos.esParticipante(nuevo, solicitud)) {
                if (!crearPermisoTemporal) {
                    throw new ApiException(ErrorCode.SIN_PERMISO_CARRERA);
                }
                permisosSolicitud
                        .findByUserIdAndSolicitudId(nuevo.getId(), solicitudId)
                        .ifPresentOrElse(
                                permiso -> permiso.reactivar(admin.getId(), expiresAt),
                                () -> permisosSolicitud.save(
                                        new PermisoSolicitud(nuevo.getId(), solicitudId, admin.getId(), expiresAt)));
                auditoria.registrar(
                        admin, "crear_permiso_temporal", nuevo, Map.of("solicitud", solicitud.getCodigoExp()), ip);
            }
            solicitud.asignarResponsable(rol, nuevo, OffsetDateTime.now());
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("usuario", admin.nombreCompleto());
            detalle.put("rol", rol);
            detalle.put("responsable", nuevo.nombreCompleto());
            detalle.put("anterior", anterior == null ? null : anterior.nombreCompleto());
            auditoria.registrarEn(admin, "asignar_responsable", "solicitud", solicitudId, detalle, motivo, ip);
            notificaciones.responsableAsignado(solicitud, nuevo, anterior);
        }
        return detalles.detalle(admin, solicitud);
    }

    // --- Alertas (RF-ADMIN-007) ---

    @Transactional(readOnly = true)
    public Alertas alertas() {
        Map<String, Estado> estados = catalogo.estadosPorCodigo();
        List<Solicitud> abiertas = solicitudes.findAll(Sort.by("id")).stream()
                .filter(s -> !estados.get(s.getEstadoCodigo()).isEsTerminal())
                .toList();
        OffsetDateTime ahora = OffsetDateTime.now();
        return new Alertas(
                abiertas.stream().filter(s -> s.getResponsableAcademico() == null).toList(),
                abiertas.stream().filter(s -> s.getResponsableAdministrativo() == null).toList(),
                Stream.concat(
                                usuarios.findByBloqueadoHastaAfter(ahora).stream(),
                                aspirantes.findByBloqueadoHastaAfter(ahora).stream())
                        .map(Cuenta.class::cast)
                        .toList());
    }

    // --- Carreras (RF-ADMIN-002) ---

    @Transactional
    public Carrera crearCarrera(
            SesionActual actual, String codigo, String nombre, String facultad, String descripcion, String ip) {
        exigirCodigoLibre(codigo.strip(), null);
        Carrera carrera = carreras.save(new Carrera(codigo.strip(), nombre.strip(), facultad.strip(), limpio(descripcion)));
        auditoria.registrarEn(
                cuentas.de(actual), "crear_carrera", "carrera", carrera.getId(), Map.of("codigo", carrera.getCodigo()), ip);
        return carrera;
    }

    @Transactional
    public Carrera editarCarrera(
            SesionActual actual, Integer id, String codigo, String nombre, String facultad, String descripcion, String ip) {
        Carrera carrera = catalogo.carrera(id);
        exigirCodigoLibre(codigo.strip(), id);
        carrera.editar(codigo.strip(), nombre.strip(), facultad.strip(), limpio(descripcion));
        auditoria.registrarEn(
                cuentas.de(actual), "editar_carrera", "carrera", id, Map.of("codigo", carrera.getCodigo()), ip);
        return carrera;
    }

    /** Baja lógica: una carrera inactiva no se ofrece en solicitudes nuevas; las que ya la tienen siguen igual. */
    @Transactional
    public Carrera activarCarrera(SesionActual actual, Integer id, boolean activo, String ip) {
        Carrera carrera = catalogo.carrera(id);
        carrera.setActivo(activo);
        auditoria.registrarEn(
                cuentas.de(actual),
                activo ? "activar_carrera" : "desactivar_carrera",
                "carrera",
                id,
                Map.of("codigo", carrera.getCodigo()),
                ip);
        return carrera;
    }

    // --- Internos ---

    private DatosUsuario datos(Usuario usuario) {
        List<PermisoTemporal> temporales = new ArrayList<>();
        for (PermisoSolicitud permiso : permisosSolicitud.vigentes(usuario.getId(), OffsetDateTime.now())) {
            temporales.add(new PermisoTemporal(permiso, solicitudes.findById(permiso.getSolicitudId()).orElseThrow()));
        }
        return new DatosUsuario(
                usuario, permisosCarrera.findByUserIdAndRevokedAtIsNullOrderByIdAsc(usuario.getId()), temporales);
    }

    private Usuario personal(Integer id) {
        return usuarios.findById(id)
                .filter(u -> !Rol.ADMIN.equals(u.rolCodigo()))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    private Rol rolDelPersonal(String rolCodigo) {
        if (rolCodigo == null || !ROLES_DEL_PERSONAL.contains(rolCodigo)) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "rolCodigo", "Elegí Académico, Secretaría o Revisor de logs.");
        }
        return roles.findByCodigo(rolCodigo).orElseThrow();
    }

    private static void exigirPassword(String password) {
        if (password == null || !password.matches(Validaciones.PASSWORD)) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "password", Validaciones.PASSWORD_INVALIDA);
        }
    }

    private void exigirCodigoLibre(String codigo, Integer propia) {
        carreras.findByCodigo(codigo).filter(c -> !c.getId().equals(propia)).ifPresent(c -> {
            throw new ApiException(ErrorCode.DUPLICADO, "Ya existe una carrera con ese código.");
        });
    }

    /** Las solicitudes abiertas de las que es responsable. */
    private List<Solicitud> procesosActivos(Usuario usuario) {
        Map<String, Estado> estados = catalogo.estadosPorCodigo();
        return solicitudes.findAll(Sort.by("id")).stream()
                .filter(s -> !estados.get(s.getEstadoCodigo()).isEsTerminal() && s.tieneComoResponsable(usuario))
                .toList();
    }

    /** El mensaje lleva los expedientes: es lo que muestra el modal antes de pedir la segunda confirmación. */
    private static void exigirSinProcesos(List<Solicitud> activos) {
        if (!activos.isEmpty()) {
            String expedientes = activos.stream().map(Solicitud::getCodigoExp).collect(Collectors.joining(", "));
            throw new ApiException(
                    ErrorCode.PROCESO_ACTIVO,
                    "Proceso activo en " + expedientes + ". Para revocar, asigne responsable a otro usuario primero.");
        }
    }

    private void quitarResponsable(Solicitud solicitud, Usuario usuario, Cuenta admin, String ip) {
        solicitud.asignarResponsable(usuario.rolCodigo(), null, OffsetDateTime.now());
        auditoria.registrarEn(
                admin,
                "quitar_responsable",
                "solicitud",
                solicitud.getId(),
                Map.of("usuario", admin.nombreCompleto(), "rol", usuario.rolCodigo(), "responsable", usuario.nombreCompleto()),
                ip);
        notificaciones.procesoSinResponsable(solicitud, usuario, admin);
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
