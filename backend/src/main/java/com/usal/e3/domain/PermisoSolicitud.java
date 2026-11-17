package com.usal.e3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Permiso temporal sobre una solicitud (Requisitos v12 §5, tabla 4). Activo si `revokedAt` es nulo y no venció.
 * Lo crea el Admin al asignar un responsable (RF-ADMIN-005, Sprint 8).
 */
@Entity
@Table(name = "user_solicitud_permissions")
public class PermisoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private Integer solicitudId;
    private OffsetDateTime assignedAt = OffsetDateTime.now();
    private Integer assignedBy;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;

    protected PermisoSolicitud() {}

    public PermisoSolicitud(Integer userId, Integer solicitudId, Integer assignedBy, OffsetDateTime expiresAt) {
        this.userId = userId;
        this.solicitudId = solicitudId;
        this.assignedBy = assignedBy;
        this.expiresAt = expiresAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
