package com.usal.e3.api.admin;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.api.solicitudes.PaginaResponse;
import com.usal.e3.api.solicitudes.SolicitudDetalleResponse;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AdminService;
import com.usal.e3.service.AdminService.Alertas;
import com.usal.e3.service.AdminService.DatosUsuario;
import com.usal.e3.service.AdminService.PermisoTemporal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Administración (contrato §9). Todo `/admin` es solo del Admin General: lo exige `SecurityConfig`. */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final int MAX_TAMANO_PAGINA = 100;

    record CrearUsuarioRequest(
            @NotBlank(message = Validaciones.REQUERIDO) @Email(message = Validaciones.EMAIL_INVALIDO) String email,
            @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 100) String nombre,
            @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 100) String apellido,
            String rolCodigo,
            String password) {}

    record EditarUsuarioRequest(
            @Size(max = 100) String nombre,
            @Size(max = 100) String apellido,
            String rolCodigo,
            Boolean activo,
            String password) {}

    record CarrerasRequest(List<Integer> carreraIds) {}

    record RevocarRequest(boolean forzar, String comentario) {}

    record ResponsablesRequest(
            Integer academicoId,
            Integer administrativoId,
            boolean crearPermisoTemporal,
            OffsetDateTime expiresAt,
            String comentario) {}

    record CarreraRef(Integer id, String nombre) {}

    record SolicitudRef(Integer id, String codigoExp) {}

    record PermisoCarreraResponse(Integer id, CarreraRef carrera, OffsetDateTime assignedAt) {

        static PermisoCarreraResponse de(PermisoCarrera p) {
            return new PermisoCarreraResponse(
                    p.getId(), new CarreraRef(p.getCarrera().getId(), p.getCarrera().getNombre()), p.getAssignedAt());
        }
    }

    record PermisoSolicitudResponse(
            Integer id, SolicitudRef solicitud, OffsetDateTime assignedAt, OffsetDateTime expiresAt) {

        static PermisoSolicitudResponse de(PermisoTemporal p) {
            return new PermisoSolicitudResponse(
                    p.permiso().getId(),
                    new SolicitudRef(p.solicitud().getId(), p.solicitud().getCodigoExp()),
                    p.permiso().getAssignedAt(),
                    p.permiso().getExpiresAt());
        }
    }

    record UsuarioAdminResponse(
            Integer id,
            String email,
            String nombre,
            String apellido,
            String rol,
            boolean activo,
            OffsetDateTime bloqueadoHasta,
            OffsetDateTime fechaUltimoLogin,
            List<PermisoCarreraResponse> permisosCarrera,
            List<PermisoSolicitudResponse> permisosSolicitud) {

        static UsuarioAdminResponse de(DatosUsuario datos) {
            Usuario u = datos.usuario();
            return new UsuarioAdminResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getNombre(),
                    u.getApellido(),
                    u.rolCodigo(),
                    u.isActivo(),
                    u.getBloqueadoHasta(),
                    u.getFechaUltimoLogin(),
                    datos.permisosCarrera().stream().map(PermisoCarreraResponse::de).toList(),
                    datos.permisosSolicitud().stream().map(PermisoSolicitudResponse::de).toList());
        }
    }

    record SolicitudAlerta(Integer id, String codigoExp, String aspirante, String carrera) {

        static SolicitudAlerta de(Solicitud s) {
            return new SolicitudAlerta(
                    s.getId(), s.getCodigoExp(), s.getAspirante().nombreCompleto(), s.getCarreraDestino().getNombre());
        }
    }

    record CuentaBloqueada(String tipo, Integer id, String nombre, String email, OffsetDateTime bloqueadoHasta) {

        static CuentaBloqueada de(Cuenta c) {
            return new CuentaBloqueada(
                    c.tipo().name().toLowerCase(), c.getId(), c.nombreCompleto(), c.getEmail(), c.getBloqueadoHasta());
        }
    }

    record AlertasResponse(
            List<SolicitudAlerta> sinResponsableAcademico,
            List<SolicitudAlerta> sinResponsableAdministrativo,
            List<CuentaBloqueada> cuentasBloqueadas) {

        static AlertasResponse de(Alertas alertas) {
            return new AlertasResponse(
                    alertas.sinResponsableAcademico().stream().map(SolicitudAlerta::de).toList(),
                    alertas.sinResponsableAdministrativo().stream().map(SolicitudAlerta::de).toList(),
                    alertas.cuentasBloqueadas().stream().map(CuentaBloqueada::de).toList());
        }
    }

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/usuarios")
    PaginaResponse<UsuarioAdminResponse> usuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_TAMANO_PAGINA));
        return PaginaResponse.de(admin.usuarios(rol, activo, q, pagina), UsuarioAdminResponse::de);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioAdminResponse crear(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody CrearUsuarioRequest body,
            HttpServletRequest request) {
        return UsuarioAdminResponse.de(admin.crear(
                actual,
                body.email(),
                body.nombre(),
                body.apellido(),
                body.rolCodigo(),
                body.password(),
                request.getRemoteAddr()));
    }

    @GetMapping("/usuarios/{id}")
    UsuarioAdminResponse usuario(@PathVariable Integer id) {
        return UsuarioAdminResponse.de(admin.usuario(id));
    }

    @PatchMapping("/usuarios/{id}")
    UsuarioAdminResponse editar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody EditarUsuarioRequest body,
            HttpServletRequest request) {
        return UsuarioAdminResponse.de(admin.editar(
                actual,
                id,
                body.nombre(),
                body.apellido(),
                body.rolCodigo(),
                body.activo(),
                body.password(),
                request.getRemoteAddr()));
    }

    /** Devuelve la cuenta con todos sus permisos activos, no solo los nuevos. */
    @PostMapping("/usuarios/{id}/permisos/carreras")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioAdminResponse asignarCarreras(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestBody CarrerasRequest body,
            HttpServletRequest request) {
        return UsuarioAdminResponse.de(admin.asignarCarreras(actual, id, body.carreraIds(), request.getRemoteAddr()));
    }

    @PostMapping("/permisos/carreras/{permisoId}/revocar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revocarCarrera(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer permisoId,
            @RequestBody RevocarRequest body,
            HttpServletRequest request) {
        admin.revocarCarrera(actual, permisoId, body.forzar(), body.comentario(), request.getRemoteAddr());
    }

    @PostMapping("/permisos/solicitudes/{permisoId}/revocar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revocarSolicitud(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer permisoId,
            @RequestBody RevocarRequest body,
            HttpServletRequest request) {
        admin.revocarSolicitud(actual, permisoId, body.comentario(), request.getRemoteAddr());
    }

    @PostMapping("/solicitudes/{id}/responsables")
    SolicitudDetalleResponse asignarResponsables(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestBody ResponsablesRequest body,
            HttpServletRequest request) {
        return SolicitudDetalleResponse.de(admin.asignarResponsables(
                actual,
                id,
                body.academicoId(),
                body.administrativoId(),
                body.crearPermisoTemporal(),
                body.expiresAt(),
                body.comentario(),
                request.getRemoteAddr()));
    }

    @GetMapping("/alertas")
    AlertasResponse alertas() {
        return AlertasResponse.de(admin.alertas());
    }
}
