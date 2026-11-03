package com.usal.e3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Permiso permanente por carrera. `revokedAt` nulo = activo (Requisitos v12 §8). */
@Entity
@Table(name = "user_career_permissions")
public class PermisoCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "career_id")
    private Carrera carrera;

    private OffsetDateTime assignedAt = OffsetDateTime.now();
    private OffsetDateTime revokedAt;

    protected PermisoCarrera() {}

    public PermisoCarrera(Integer userId, Carrera carrera) {
        this.userId = userId;
        this.carrera = carrera;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
