package com.usal.e3.repository;

import com.usal.e3.domain.EventoAuditoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    List<EventoAuditoria> findByUsuarioEmail(String usuarioEmail);
}
