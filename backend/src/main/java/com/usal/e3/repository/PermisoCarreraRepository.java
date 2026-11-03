package com.usal.e3.repository;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.PermisoCarrera;
import java.util.List;
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
}
