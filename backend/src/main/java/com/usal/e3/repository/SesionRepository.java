package com.usal.e3.repository;

import com.usal.e3.domain.Sesion;
import com.usal.e3.domain.TitularTipo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SesionRepository extends JpaRepository<Sesion, UUID> {

    List<Sesion> findTop10ByTitularTipoAndTitularIdOrderByInicioDesc(TitularTipo tipo, Integer id);

    List<Sesion> findByTitularTipoAndTitularIdAndCerradaEnIsNullAndExpiraEnAfterOrderByUltimoUsoDesc(
            TitularTipo tipo, Integer id, OffsetDateTime ahora);

    @Modifying
    @Query("""
            update Sesion s set s.cerradaEn = :ahora
            where s.titularTipo = :tipo and s.titularId = :id and s.cerradaEn is null and s.id <> :salvo
            """)
    void cerrarOtras(TitularTipo tipo, Integer id, UUID salvo, OffsetDateTime ahora);

    @Modifying
    @Query("""
            update Sesion s set s.cerradaEn = :ahora
            where s.titularTipo = :tipo and s.titularId = :id and s.cerradaEn is null
            """)
    void cerrarTodas(TitularTipo tipo, Integer id, OffsetDateTime ahora);
}
