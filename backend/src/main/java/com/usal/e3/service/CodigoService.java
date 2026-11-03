package com.usal.e3.service;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.CodigoVerificacion;
import com.usal.e3.domain.PropositoCodigo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.CodigoVerificacionRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Códigos de 6 dígitos: vencen a los 10 minutos y 3 fallos bloquean 15 minutos (RF-AUTH-001). */
@Service
public class CodigoService {

    static final Duration VIGENCIA = Duration.ofMinutes(10);
    static final Duration BLOQUEO = Duration.ofMinutes(15);
    private static final int MAX_INTENTOS = 3;

    private final CodigoVerificacionRepository codigos;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public CodigoService(CodigoVerificacionRepository codigos, PasswordEncoder passwordEncoder) {
        this.codigos = codigos;
        this.passwordEncoder = passwordEncoder;
    }

    /** Invalida el anterior, pero hereda sus fallos: reenviar no da intentos nuevos. */
    public String generar(Aspirante aspirante, PropositoCodigo proposito) {
        OffsetDateTime ahora = OffsetDateTime.now();
        int intentos = 0;
        var anterior = vigente(aspirante, proposito);
        if (anterior != null) {
            intentos = anterior.getIntentos();
            anterior.setUsadoEn(ahora);
        }
        String codigo = "%06d".formatted(random.nextInt(1_000_000));
        codigos.save(new CodigoVerificacion(
                aspirante.getId(), proposito, passwordEncoder.encode(codigo), ahora.plus(VIGENCIA), intentos));
        return codigo;
    }

    /** Consume el código o tira el error del contrato. Llamar con la fila del aspirante bloqueada. */
    public void verificar(Aspirante aspirante, PropositoCodigo proposito, String codigo) {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (aspirante.getBloqueadoHasta() != null && aspirante.getBloqueadoHasta().isAfter(ahora)) {
            throw ApiException.bloqueada(ErrorCode.CUENTA_BLOQUEADA.mensaje(), aspirante.getBloqueadoHasta());
        }
        CodigoVerificacion vigente = vigente(aspirante, proposito);
        if (vigente == null) {
            throw new ApiException(ErrorCode.CODIGO_INVALIDO);
        }
        if (vigente.getExpiraEn().isBefore(ahora)) {
            throw new ApiException(ErrorCode.CODIGO_EXPIRADO);
        }
        if (!passwordEncoder.matches(codigo, vigente.getCodigoHash())) {
            throw registrarFallo(aspirante, vigente, ahora);
        }
        vigente.setUsadoEn(ahora);
    }

    private ApiException registrarFallo(Aspirante aspirante, CodigoVerificacion vigente, OffsetDateTime ahora) {
        int intentos = vigente.getIntentos() + 1;
        vigente.setIntentos(intentos);
        if (intentos >= MAX_INTENTOS) {
            vigente.setUsadoEn(ahora);
            OffsetDateTime hasta = ahora.plus(BLOQUEO);
            aspirante.setBloqueadoHasta(hasta);
            return ApiException.bloqueada("Tu cuenta está bloqueada por 15 minutos.", hasta);
        }
        return ApiException.conIntentos(ErrorCode.CODIGO_INVALIDO, MAX_INTENTOS - intentos);
    }

    private CodigoVerificacion vigente(Aspirante aspirante, PropositoCodigo proposito) {
        return codigos.findFirstByAspiranteIdAndPropositoAndUsadoEnIsNullOrderByIdDesc(aspirante.getId(), proposito)
                .orElse(null);
    }
}
