package com.usal.e3.repository;

import com.usal.e3.domain.CuponPago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponPagoRepository extends JpaRepository<CuponPago, Integer> {

    Optional<CuponPago> findBySolicitudId(Integer solicitudId);
}
