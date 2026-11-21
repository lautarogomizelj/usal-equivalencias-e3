package com.usal.e3.repository;

import com.usal.e3.domain.EventoAuditoria;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    List<EventoAuditoria> findByUsuarioEmail(String usuarioEmail);

    /** Lo que pasó sobre una entidad puntual, por ejemplo los cambios de responsable de una solicitud. */
    List<EventoAuditoria> findByEntidadAndEntidadIdAndAccionIn(
            String entidad, Integer entidadId, Collection<String> acciones);
}
