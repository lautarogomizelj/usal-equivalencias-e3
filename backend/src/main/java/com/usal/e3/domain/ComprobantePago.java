package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Fila de `comprobantes_pago` (contrato §2.15): el comprobante que carga el aspirante (RF-ASP-007) y que
 * Secretaría verifica (RF-SEC-002). Cada reenvío es una versión nueva; las anteriores se conservan.
 */
@Entity
@Table(name = "comprobantes_pago")
public class ComprobantePago {

    public enum Verificacion {
        PENDIENTE,
        ACEPTADO,
        RECHAZADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private Integer cuponId;
    private int version;
    private String nombreArchivo;
    private String rutaArchivo;
    private Integer tamanoBytes;
    private OffsetDateTime fechaCarga;

    @Enumerated(EnumType.STRING)
    private Verificacion estadoVerificacion = Verificacion.PENDIENTE;

    private String comentarioSecretaria;
    private Integer verificadoPorId;
    private OffsetDateTime fechaVerificacion;

    protected ComprobantePago() {}

    public ComprobantePago(
            CuponPago cupon, int version, String nombreArchivo, String rutaArchivo, int tamanoBytes, OffsetDateTime fecha) {
        this.solicitudId = cupon.getSolicitudId();
        this.cuponId = cupon.getId();
        this.version = version;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.tamanoBytes = tamanoBytes;
        this.fechaCarga = fecha;
    }

    public void verificar(Verificacion resultado, String comentario, Usuario secretaria, OffsetDateTime fecha) {
        this.estadoVerificacion = resultado;
        this.comentarioSecretaria = comentario;
        this.verificadoPorId = secretaria.getId();
        this.fechaVerificacion = fecha;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSolicitudId() {
        return solicitudId;
    }

    public int getVersion() {
        return version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public Integer getTamanoBytes() {
        return tamanoBytes;
    }

    public OffsetDateTime getFechaCarga() {
        return fechaCarga;
    }

    public Verificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public String getComentarioSecretaria() {
        return comentarioSecretaria;
    }
}
