package com.usal.e3.repository;

import com.usal.e3.domain.Estado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    /** En el orden de RF-ESTADO-001. */
    List<Estado> findAllByOrderByIdAsc();
}
