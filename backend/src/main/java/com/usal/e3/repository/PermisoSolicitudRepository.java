package com.usal.e3.repository;

import com.usal.e3.domain.PermisoSolicitud;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermisoSolicitudRepository extends JpaRepository<PermisoSolicitud, Integer> {

    /** Requisitos v12 §6, paso 1: sin vencer y con `revoked_at IS NULL`. */
    @Query("""
            select count(p) > 0 from PermisoSolicitud p
            where p.userId = :userId and p.solicitudId = :solicitudId and p.revokedAt is null
              and (p.expiresAt is null or p.expiresAt > :ahora)
            """)
    boolean activo(Integer userId, Integer solicitudId, OffsetDateTime ahora);

    @Query("""
            select p from PermisoSolicitud p
            where p.userId = :userId and p.revokedAt is null and (p.expiresAt is null or p.expiresAt > :ahora)
            order by p.id
            """)
    List<PermisoSolicitud> vigentes(Integer userId, OffsetDateTime ahora);

    /** Con o sin revocar: la tabla tiene una sola fila por usuario y solicitud. */
    Optional<PermisoSolicitud> findByUserIdAndSolicitudId(Integer userId, Integer solicitudId);
}
