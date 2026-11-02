package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.UsuarioRepository;
import com.usal.e3.security.SesionActual;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {

    private final UsuarioRepository usuarios;
    private final AspiranteRepository aspirantes;
    private final AuditoriaService auditoria;

    public CuentaService(UsuarioRepository usuarios, AspiranteRepository aspirantes, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.aspirantes = aspirantes;
        this.auditoria = auditoria;
    }

    public Optional<Cuenta> buscar(TitularTipo tipo, Integer id) {
        return tipo == TitularTipo.USUARIO
                ? usuarios.findById(id).map(Cuenta.class::cast)
                : aspirantes.findById(id).map(Cuenta.class::cast);
    }

    /** RF-AUTH-002: el Admin desbloquea y el contador vuelve a cero. */
    @Transactional
    public void desbloquear(SesionActual actual, TitularTipo tipo, Integer id, String ip) {
        Cuenta cuenta = buscar(tipo, id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        cuenta.setIntentosLoginFallidos(0);
        cuenta.setBloqueadoHasta(null);
        cuenta.setFechaUltimoIntentoFallido(null);
        Cuenta actor = buscar(actual.tipo(), actual.id()).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        auditoria.registrar(actor, "desbloqueo_cuenta", cuenta, null, ip);
    }
}
