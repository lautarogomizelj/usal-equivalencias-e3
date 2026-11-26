package com.usal.e3.repository;

import com.usal.e3.domain.InformeMaterias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeMateriasRepository extends JpaRepository<InformeMaterias, Integer> {

    Optional<InformeMaterias> findBySolicitudId(Integer solicitudId);
}
