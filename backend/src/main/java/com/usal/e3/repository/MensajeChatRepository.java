package com.usal.e3.repository;

import com.usal.e3.domain.MensajeChat;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Integer> {

    /** Del más viejo al más nuevo. Con `despuesDe` en 0 trae el historial completo. */
    Page<MensajeChat> findBySolicitudIdAndIdGreaterThanOrderByIdAsc(
            Integer solicitudId, Integer despuesDe, Pageable pagina);

    List<MensajeChat> findBySolicitudIdAndIdGreaterThan(Integer solicitudId, Integer despuesDe);
}
