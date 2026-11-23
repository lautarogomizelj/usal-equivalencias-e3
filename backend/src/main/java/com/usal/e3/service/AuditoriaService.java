package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.EventoAuditoria;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AuditoriaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Traza de RF-AUDIT-001. Corre en la transacción de quien la llama. */
@Service
public class AuditoriaService {

    /** Filtros de `GET /auditoria` (contrato §8.3). `usuario` busca dentro del email; los días son de Argentina. */
    public record Filtro(
            String usuario, String accion, String entidad, Integer entidadId, LocalDate desde, LocalDate hasta) {}

    public record Opciones(List<String> acciones, List<String> entidades) {}

    private final AuditoriaRepository auditoria;

    public AuditoriaService(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    /** La propia cuenta actúa sobre sí misma: login, cambio de contraseña, perfil… */
    public void registrar(Cuenta cuenta, String accion, Map<String, Object> detalles, String ip) {
        registrar(cuenta, accion, cuenta, detalles, ip);
    }

    public void registrar(Cuenta actor, String accion, Cuenta objetivo, Map<String, Object> detalles, String ip) {
        auditoria.save(new EventoAuditoria(
                usuarioId(actor),
                actor.getEmail(),
                accion,
                objetivo.tipo().name().toLowerCase(),
                objetivo.getId(),
                detalles,
                ip));
    }

    /** Lo que alguien hace sobre una solicitud o un documento sin cambiar el estado: tomar el proceso, marcar… */
    public void registrarEn(
            Cuenta actor, String accion, String entidad, Integer entidadId, Map<String, Object> detalles, String ip) {
        auditoria.save(new EventoAuditoria(usuarioId(actor), actor.getEmail(), accion, entidad, entidadId, detalles, ip));
    }

    /** Siempre por código, nunca por nombre de estado (RF-ESTADO-004). */
    public void registrarCambioEstado(
            Cuenta actor, Integer solicitudId, String anterior, String nuevo, String comentario, String ip) {
        auditoria.save(EventoAuditoria.cambioEstado(
                usuarioId(actor), actor.getEmail(), solicitudId, anterior, nuevo, comentario, ip));
    }

    /** Lo que hace el Admin con un motivo escrito: asignar un responsable o revocar un permiso. */
    public void registrarEn(
            Cuenta actor,
            String accion,
            String entidad,
            Integer entidadId,
            Map<String, Object> detalles,
            String comentario,
            String ip) {
        auditoria.save(
                new EventoAuditoria(usuarioId(actor), actor.getEmail(), accion, entidad, entidadId, detalles, ip)
                        .conComentario(comentario));
    }

    /** RF-AUDIT-003: lo más nuevo primero. */
    @Transactional(readOnly = true)
    public Page<EventoAuditoria> buscar(Filtro filtro, Pageable pagina) {
        Specification<EventoAuditoria> condicion = (evento, consulta, cb) -> {
            List<Predicate> partes = new ArrayList<>();
            if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
                String parte = "%" + filtro.usuario().strip().toLowerCase(Locale.ROOT) + "%";
                partes.add(cb.like(cb.lower(evento.get("usuarioEmail")), parte));
            }
            if (filtro.accion() != null && !filtro.accion().isBlank()) {
                partes.add(cb.equal(evento.get("accion"), filtro.accion()));
            }
            if (filtro.entidad() != null && !filtro.entidad().isBlank()) {
                partes.add(cb.equal(evento.get("entidad"), filtro.entidad()));
            }
            if (filtro.entidadId() != null) {
                partes.add(cb.equal(evento.get("entidadId"), filtro.entidadId()));
            }
            if (filtro.desde() != null) {
                partes.add(cb.greaterThanOrEqualTo(
                        evento.get("fecha"),
                        filtro.desde().atStartOfDay(ProcesoService.ZONA).toOffsetDateTime()));
            }
            if (filtro.hasta() != null) {
                partes.add(cb.lessThan(
                        evento.get("fecha"),
                        filtro.hasta().plusDays(1).atStartOfDay(ProcesoService.ZONA).toOffsetDateTime()));
            }
            return cb.and(partes.toArray(Predicate[]::new));
        };
        return auditoria.findAll(
                condicion,
                PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public EventoAuditoria evento(Long id) {
        return auditoria.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    @Transactional(readOnly = true)
    public Opciones opciones() {
        return new Opciones(auditoria.acciones(), auditoria.entidades());
    }

    private static Integer usuarioId(Cuenta actor) {
        return actor.tipo() == TitularTipo.USUARIO ? actor.getId() : null;
    }
}
