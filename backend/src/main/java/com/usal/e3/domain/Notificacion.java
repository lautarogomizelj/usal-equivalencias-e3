package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Aviso de la campana (contrato §2.21): el mismo texto que sale por mail, para quien lo recibe. */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TitularTipo destinatarioTipo;

    private Integer destinatarioId;
    private Integer solicitudId;
    private String titulo;
    private String mensaje;
    private boolean leida;
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    protected Notificacion() {}

    public Notificacion(Cuenta destinatario, Integer solicitudId, String titulo, String mensaje) {
        this.destinatarioTipo = destinatario.tipo();
        this.destinatarioId = destinatario.getId();
        this.solicitudId = solicitudId;
        this.titulo = titulo;
        this.mensaje = mensaje;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public Long getId() {
        return id;
    }

    public Integer getSolicitudId() {
        return solicitudId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isLeida() {
        return leida;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
