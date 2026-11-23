package com.usal.e3.repository;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.PermisoCarrera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermisoCarreraRepository extends JpaRepository<PermisoCarrera, Integer> {

    /** Requisitos v12 §8: `revoked_at IS NULL` siempre. */
    @Query("""
            select p.carrera from PermisoCarrera p
            where p.userId = :userId and p.revokedAt is null
            order by p.carrera.nombre
            """)
    List<Carrera> carrerasActivas(Integer userId);

    /** Requisitos v12 §6, paso 2. */
    @Query("""
            select count(p) > 0 from PermisoCarrera p
            where p.userId = :userId and p.carrera.id = :carreraId and p.revokedAt is null
            """)
    boolean activo(Integer userId, Integer carreraId);

    List<PermisoCarrera> findByUserIdAndRevokedAtIsNullOrderByIdAsc(Integer userId);

    /** Con o sin revocar: la tabla tiene una sola fila por usuario y carrera. */
    Optional<PermisoCarrera> findByUserIdAndCarrera_Id(Integer userId, Integer carreraId);
}
