package com.usal.e3.api.admin;

import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.CuentaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Desbloqueo de cuentas (RF-AUTH-002, contrato §9). El resto de `/admin` llega en el Sprint 8. */
@RestController
@RequestMapping("/admin/cuentas")
public class CuentasController {

    private final CuentaService cuentas;

    public CuentasController(CuentaService cuentas) {
        this.cuentas = cuentas;
    }

    @PostMapping("/{tipo}/{id}/desbloquear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desbloquear(
            @AuthenticationPrincipal SesionActual actual,
            @PathVariable String tipo,
            @PathVariable Integer id,
            HttpServletRequest request) {
        cuentas.desbloquear(actual, titular(tipo), id, request.getRemoteAddr());
    }

    private static TitularTipo titular(String tipo) {
        return switch (tipo) {
            case "aspirante" -> TitularTipo.ASPIRANTE;
            case "usuario" -> TitularTipo.USUARIO;
            default -> throw new ApiException(ErrorCode.NO_ENCONTRADO);
        };
    }
}
