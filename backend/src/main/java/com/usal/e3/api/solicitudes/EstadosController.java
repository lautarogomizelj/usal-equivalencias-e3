package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.Estado;
import com.usal.e3.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo de los 21 estados (contrato §5, RF-ESTADO-003). */
@RestController
@RequestMapping("/estados")
public class EstadosController {

    private final CatalogoService catalogo;

    public EstadosController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    public record EstadoResponse(
            String codigo, String nombre, String descripcion, boolean esTerminal, boolean activo) {

        static EstadoResponse de(Estado estado) {
            return new EstadoResponse(
                    estado.getCodigo(),
                    estado.getNombre(),
                    estado.getDescripcion(),
                    estado.isEsTerminal(),
                    estado.isActivo());
        }
    }

    @GetMapping
    List<EstadoResponse> listar() {
        return catalogo.estados().stream().map(EstadoResponse::de).toList();
    }
}
