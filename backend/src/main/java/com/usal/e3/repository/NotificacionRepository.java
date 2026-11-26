package com.usal.e3.repository;

import com.usal.e3.domain.Notificacion;
import com.usal.e3.domain.TitularTipo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Las más nuevas primero. Con `leida` nulo trae todas. */
    @Query("""
            select n from Notificacion n
            where n.destinatarioTipo = :tipo and n.destinatarioId = :id
              and (:leida is null or n.leida = :leida)
            order by n.id desc
            """)
    Page<Notificacion> deCuenta(TitularTipo tipo, Integer id, Boolean leida, Pageable pagina);

    long countByDestinatarioTipoAndDestinatarioIdAndLeidaFalse(TitularTipo tipo, Integer id);

    Optional<Notificacion> findByIdAndDestinatarioTipoAndDestinatarioId(Long id, TitularTipo tipo, Integer destinatarioId);

    @Modifying
    @Query("""
            update Notificacion n set n.leida = true
            where n.destinatarioTipo = :tipo and n.destinatarioId = :id and n.leida = false
            """)
    void marcarTodasLeidas(TitularTipo tipo, Integer id);
}
