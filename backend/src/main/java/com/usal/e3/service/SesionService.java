package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Sesion;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.SesionRepository;
import com.usal.e3.security.JwtService;
import com.usal.e3.security.SesionActual;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sesión = JWT firmado en cookie HttpOnly + fila en `sesiones`. La firma evita tocar datos falsos y la
 * fila permite cortar el acceso al instante: logout, "Sesiones activas" o cuenta desactivada.
 */
@Service
public class SesionService {

    public static final Duration NORMAL = Duration.ofDays(1);
    public static final Duration RECORDARME = Duration.ofDays(30);
    private static final Duration PRECISION_ULTIMO_USO = Duration.ofMinutes(1);
    private static final int MAX_USER_AGENT = 500;

    private final SesionRepository sesiones;
    private final CuentaService cuentas;
    private final PermisosService permisos;
    private final JwtService jwt;

    public SesionService(
            SesionRepository sesiones, CuentaService cuentas, PermisosService permisos, JwtService jwt) {
        this.sesiones = sesiones;
        this.cuentas = cuentas;
        this.permisos = permisos;
        this.jwt = jwt;
    }

    public SesionIniciada iniciar(Cuenta cuenta, Duration duracion, ClienteHttp cliente) {
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime expiraEn = ahora.plus(duracion);
        UUID id = UUID.randomUUID();
        sesiones.save(new Sesion(
                id, cuenta.tipo(), cuenta.getId(), cliente.ip(), recortar(cliente.userAgent()), ahora, expiraEn));
        cuenta.setFechaUltimoLogin(ahora);
        return new SesionIniciada(jwt.firmar(id, expiraEn), duracion, datos(cuenta, expiraEn));
    }

    /** Lo usa el filtro en cada pedido: una sesión cerrada o una cuenta inactiva dejan de valer ya. */
    @Transactional
    public Optional<SesionActual> resolver(String token) {
        return jwt.leer(token).flatMap(sesiones::findById).filter(this::abierta).flatMap(sesion -> {
            Optional<Cuenta> cuenta = cuentas.buscar(sesion.getTitularTipo(), sesion.getTitularId())
                    .filter(Cuenta::isActivo);
            cuenta.ifPresent(c -> marcarUso(sesion));
            return cuenta.map(c -> new SesionActual(
                    sesion.getId(), c.tipo(), c.getId(), c.getEmail(), c.rolCodigo(), sesion.getExpiraEn()));
        });
    }

    @Transactional(readOnly = true)
    public DatosSesion datos(SesionActual actual) {
        Cuenta cuenta = cuentas.buscar(actual.tipo(), actual.id())
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        return datos(cuenta, actual.expiraEn());
    }

    @Transactional
    public void cerrar(SesionActual actual, UUID sesionId) {
        Sesion sesion = sesiones.findById(sesionId)
                .filter(s -> s.getTitularTipo() == actual.tipo() && s.getTitularId().equals(actual.id()))
                .filter(this::abierta)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        sesion.setCerradaEn(OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Sesion> ultimosAccesos(SesionActual actual) {
        return sesiones.findTop10ByTitularTipoAndTitularIdOrderByInicioDesc(actual.tipo(), actual.id());
    }

    @Transactional(readOnly = true)
    public List<Sesion> activas(SesionActual actual) {
        return sesiones.findByTitularTipoAndTitularIdAndCerradaEnIsNullAndExpiraEnAfterOrderByUltimoUsoDesc(
                actual.tipo(), actual.id(), OffsetDateTime.now());
    }

    /** Tras cambiar la contraseña solo sobrevive la sesión desde la que se cambió. */
    void cerrarOtras(SesionActual actual) {
        sesiones.cerrarOtras(actual.tipo(), actual.id(), actual.sesionId(), OffsetDateTime.now());
    }

    void cerrarTodas(TitularTipo tipo, Integer id) {
        sesiones.cerrarTodas(tipo, id, OffsetDateTime.now());
    }

    private DatosSesion datos(Cuenta cuenta, OffsetDateTime expiraEn) {
        return new DatosSesion(cuenta, permisos.carrerasConPermiso(cuenta), expiraEn);
    }

    private boolean abierta(Sesion sesion) {
        return sesion.getCerradaEn() == null && sesion.getExpiraEn().isAfter(OffsetDateTime.now());
    }

    private void marcarUso(Sesion sesion) {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (sesion.getUltimoUso().plus(PRECISION_ULTIMO_USO).isBefore(ahora)) {
            sesion.setUltimoUso(ahora);
        }
    }

    private static String recortar(String userAgent) {
        return userAgent != null && userAgent.length() > MAX_USER_AGENT
                ? userAgent.substring(0, MAX_USER_AGENT)
                : userAgent;
    }
}
