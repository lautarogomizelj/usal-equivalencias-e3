package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `estados_solicitud` (RF-ESTADO-003): un cambio de estado, siempre por código. */
@Entity
@Table(name = "estados_solicitud")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private String estadoCodigo;
    private String estadoAnteriorCodigo;
    private OffsetDateTime fechaAsignacion;
    private OffsetDateTime fechaUltimaModificacion;

    /** Nulo si el cambio lo disparó el aspirante. */
    @ManyToOne
    @JoinColumn(name = "usuario_ultimo_cambio_id")
    private Usuario usuarioUltimoCambio;

    private String comentarios;

    protected HistorialEstado() {}

    public HistorialEstado(
            Integer solicitudId,
            String estadoCodigo,
            String estadoAnteriorCodigo,
            Usuario usuarioUltimoCambio,
            String comentarios,
            OffsetDateTime fecha) {
        this.solicitudId = solicitudId;
        this.estadoCodigo = estadoCodigo;
        this.estadoAnteriorCodigo = estadoAnteriorCodigo;
        this.usuarioUltimoCambio = usuarioUltimoCambio;
        this.comentarios = comentarios;
        this.fechaAsignacion = fecha;
        this.fechaUltimaModificacion = fecha;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getEstadoAnteriorCodigo() {
        return estadoAnteriorCodigo;
    }

    public OffsetDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public Usuario getUsuarioUltimoCambio() {
        return usuarioUltimoCambio;
    }

    public String getComentarios() {
        return comentarios;
    }
}
