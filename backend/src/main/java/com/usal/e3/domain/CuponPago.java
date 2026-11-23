package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Fila de `cupones_pago` (contrato §2.14): el cupón en PDF que Secretaría genera y envía (RF-SEC-001). */
@Entity
@Table(name = "cupones_pago")
public class CuponPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private BigDecimal monto;
    private String rutaArchivo;
    private Integer emitidoPorId;
    private OffsetDateTime fechaEmision;

    protected CuponPago() {}

    public CuponPago(
            Integer solicitudId, BigDecimal monto, String rutaArchivo, Usuario emitidoPor, OffsetDateTime fecha) {
        this.solicitudId = solicitudId;
        this.monto = monto;
        this.rutaArchivo = rutaArchivo;
        this.emitidoPorId = emitidoPor.getId();
        this.fechaEmision = fecha;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSolicitudId() {
        return solicitudId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public OffsetDateTime getFechaEmision() {
        return fechaEmision;
    }
}
