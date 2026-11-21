package com.usal.e3.repository;

import com.usal.e3.domain.Reunion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReunionRepository extends JpaRepository<Reunion, Integer> {

    /** La reunión vigente de la solicitud. */
    Optional<Reunion> findFirstBySolicitudIdOrderByIdDesc(Integer solicitudId);
}
