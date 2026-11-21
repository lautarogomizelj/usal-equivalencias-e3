package com.usal.e3.repository;

import com.usal.e3.domain.RondaReunion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RondaReunionRepository extends JpaRepository<RondaReunion, Integer> {

    /** En el orden en que se propusieron: la vigente es la última. */
    List<RondaReunion> findBySolicitudIdOrderByIdAsc(Integer solicitudId);
}
