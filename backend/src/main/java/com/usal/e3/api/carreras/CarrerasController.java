package com.usal.e3.api.carreras;

import com.usal.e3.api.auth.Validaciones;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Rol;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AdminService;
import com.usal.e3.service.CatalogoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo de carreras (contrato §5). Leen todos; el alta, la edición y la baja lógica son del Admin (RF-ADMIN-002). */
@RestController
@RequestMapping("/carreras")
public class CarrerasController {

    private final CatalogoService catalogo;
    private final AdminService admin;

    public CarrerasController(CatalogoService catalogo, AdminService admin) {
        this.catalogo = catalogo;
        this.admin = admin;
    }

    record CarreraRequest(
            @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 50) String codigo,
            @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String nombre,
            @NotBlank(message = Validaciones.REQUERIDO) @Size(max = 255) String facultad,
            @Size(max = 2000) String descripcion) {}

    record ActivoRequest(boolean activo) {}

    public record CarreraResponse(
            Integer id, String codigo, String nombre, String facultad, String descripcion, boolean activo) {

        static CarreraResponse de(Carrera carrera) {
            return new CarreraResponse(
                    carrera.getId(),
                    carrera.getCodigo(),
                    carrera.getNombre(),
                    carrera.getFacultad(),
                    carrera.getDescripcion(),
                    carrera.isActivo());
        }
    }

    @GetMapping
    List<CarreraResponse> listar(
            @AuthenticationPrincipal SesionActual actual, @RequestParam(defaultValue = "true") String activo) {
        boolean todas = "todas".equals(activo) && Rol.ADMIN.equals(actual.rol());
        return catalogo.carreras(todas).stream().map(CarreraResponse::de).toList();
    }

    @GetMapping("/{id}")
    CarreraResponse detalle(@PathVariable Integer id) {
        return CarreraResponse.de(catalogo.carrera(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CarreraResponse crear(
            @AuthenticationPrincipal SesionActual actual,
            @Valid @RequestBody CarreraRequest body,
            HttpServletRequest request) {
        return CarreraResponse.de(admin.crearCarrera(
                actual, body.codigo(), body.nombre(), body.facultad(), body.descripcion(), request.getRemoteAddr()));
    }

    @PutMapping("/{id}")
    CarreraResponse editar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @Valid @RequestBody CarreraRequest body,
            HttpServletRequest request) {
        return CarreraResponse.de(admin.editarCarrera(
                actual, id, body.codigo(), body.nombre(), body.facultad(), body.descripcion(), request.getRemoteAddr()));
    }

    @PatchMapping("/{id}/activo")
    CarreraResponse activar(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable Integer id,
            @RequestBody ActivoRequest body,
            HttpServletRequest request) {
        return CarreraResponse.de(admin.activarCarrera(actual, id, body.activo(), request.getRemoteAddr()));
    }
}
