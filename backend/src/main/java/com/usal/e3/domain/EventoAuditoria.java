package com.usal.e3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Fila de `auditoria_sistema` (RF-AUDIT-001). */
@Entity
@Table(name = "auditoria_sistema")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private OffsetDateTime fecha = OffsetDateTime.now();

    private Integer usuarioId;
    private String usuarioEmail;
    private String accion;
    private String entidad;
    private Integer entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> detalles;

    private String ipOrigen;

    protected EventoAuditoria() {}

    public EventoAuditoria(
            Integer usuarioId,
            String usuarioEmail,
            String accion,
            String entidad,
            Integer entidadId,
            Map<String, Object> detalles,
            String ipOrigen) {
        this.usuarioId = usuarioId;
        this.usuarioEmail = usuarioEmail;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalles = detalles;
        this.ipOrigen = ipOrigen;
    }

    public String getAccion() {
        return accion;
    }
}
