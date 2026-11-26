package com.usal.e3.repository;

import com.usal.e3.domain.ComprobantePago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    /** Del primero al último: el vigente es el de mayor versión. */
    List<ComprobantePago> findBySolicitudIdOrderByVersionAsc(Integer solicitudId);
}
