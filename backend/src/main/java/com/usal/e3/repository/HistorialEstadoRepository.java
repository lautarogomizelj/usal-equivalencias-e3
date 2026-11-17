package com.usal.e3.repository;

import com.usal.e3.domain.HistorialEstado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    /** RF-AUDIT-002: el más reciente primero. */
    List<HistorialEstado> findBySolicitudIdOrderByFechaAsignacionDescIdDesc(Integer solicitudId);
}
