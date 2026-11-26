package com.usal.e3.repository;

import com.usal.e3.domain.EventoAuditoria;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AuditoriaRepository 
        extends JpaRepository<EventoAuditoria, Long>, JpaSpecificationExecutor<EventoAuditoria> {

    List<EventoAuditoria> findByUsuarioEmail(String usuarioEmail);

    /** Lo que pasó sobre una entidad puntual, por ejemplo los cambios de responsable de una solicitud. */
    List<EventoAuditoria> findByEntidadAndEntidadIdAndAccionIn(
            String entidad, Integer entidadId, Collection<String> acciones);

    /** Para armar el filtro "Acción" de la pantalla de auditoría con lo que realmente hay registrado. */
    @Query("select distinct e.accion from EventoAuditoria e order by e.accion")
    List<String> acciones();

    @Query("select distinct e.entidad from EventoAuditoria e order by e.entidad")
    List<String> entidades();
}
