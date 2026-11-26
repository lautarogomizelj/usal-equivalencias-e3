package com.usal.e3.service;

import com.usal.e3.domain.Notificacion;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.NotificacionRepository;
import com.usal.e3.security.SesionActual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** La campana (contrato §8.2): cada uno lee y marca solo sus avisos. Los crea `NotificacionService`. */
@Service
public class CampanaService {

    private final NotificacionRepository notificaciones;

    public CampanaService(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Transactional(readOnly = true)
    public Page<Notificacion> listar(SesionActual actual, Boolean leida, Pageable pagina) {
        return notificaciones.deCuenta(actual.tipo(), actual.id(), leida, pagina);
    }

    @Transactional(readOnly = true)
    public long noLeidas(SesionActual actual) {
        return notificaciones.countByDestinatarioTipoAndDestinatarioIdAndLeidaFalse(actual.tipo(), actual.id());
    }

    @Transactional
    public void marcarLeida(SesionActual actual, Long id) {
        notificaciones
                .findByIdAndDestinatarioTipoAndDestinatarioId(id, actual.tipo(), actual.id())
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO))
                .marcarLeida();
    }

    @Transactional
    public void marcarTodasLeidas(SesionActual actual) {
        notificaciones.marcarTodasLeidas(actual.tipo(), actual.id());
    }
}
