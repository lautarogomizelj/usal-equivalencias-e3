package com.usal.e3.repository;

import com.usal.e3.domain.AnalisisAcademico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalisisRepository extends JpaRepository<AnalisisAcademico, Integer> {

    /** La versión más nueva primero: la vigente de cada tipo es la primera que aparece. */
    List<AnalisisAcademico> findBySolicitudIdOrderByVersionDescIdAsc(Integer solicitudId);
}
