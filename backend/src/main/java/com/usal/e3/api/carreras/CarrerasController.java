package com.usal.e3.api.carreras;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Rol;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.CatalogoService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Lectura del catálogo de carreras (contrato §5). El alta y la edición del Admin llegan en el Sprint 8. */
@RestController
@RequestMapping("/carreras")
public class CarrerasController {

    private final CatalogoService catalogo;

    public CarrerasController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

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
}
