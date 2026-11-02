package com.usal.e3.repository;

import com.usal.e3.domain.CodigoVerificacion;
import com.usal.e3.domain.PropositoCodigo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Integer> {

    Optional<CodigoVerificacion> findFirstByAspiranteIdAndPropositoAndUsadoEnIsNullOrderByIdDesc(
            Integer aspiranteId, PropositoCodigo proposito);
}
