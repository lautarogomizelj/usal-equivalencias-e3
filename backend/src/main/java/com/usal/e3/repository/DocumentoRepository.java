package com.usal.e3.repository;

import com.usal.e3.domain.DocumentoSolicitud;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<DocumentoSolicitud, Integer> {

    List<DocumentoSolicitud> findBySolicitudIdOrderByIdAsc(Integer solicitudId);

    Optional<DocumentoSolicitud> findByIdAndSolicitudId(Integer id, Integer solicitudId);
}
