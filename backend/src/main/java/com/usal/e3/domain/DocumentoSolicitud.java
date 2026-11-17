package com.usal.e3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `documentos_solicitud` (RF-ACAD-009, contrato §2.8). Las versiones anteriores no se borran. */
@Entity
@Table(name = "documentos_solicitud")
public class DocumentoSolicitud {

    public enum Tipo {
        ANALITICO,
        PLAN,
        PROGRAMA,
        OTRO
    }

    public enum EstadoRevision {
        PENDIENTE,
        OK,
        RECARGAR
    }

    public enum EtapaRevision {
        DOCUMENTACION,
        LEGALIZACION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private String descripcion;
    private String nombreArchivo;
    private String rutaArchivo;
    private Integer tamanoBytes;
    private OffsetDateTime fechaCarga;
    private int version = 1;

    @Column(name = "reemplaza_a_id")
    private Integer reemplazaAId;

    @Enumerated(EnumType.STRING)
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private EtapaRevision etapaRevision = EtapaRevision.DOCUMENTACION;

    private String comentarioRevision;

    @ManyToOne
    @JoinColumn(name = "revisado_por_id")
    private Usuario revisadoPor;

    private OffsetDateTime fechaRevision;
    private boolean esRequeridoFaltante;

    /** Propuesta del contrato: `true` hasta que el aspirante ejecuta ENVIAR_DOCUMENTACION. */
    private boolean borrador = true;

    protected DocumentoSolicitud() {}

    public DocumentoSolicitud(
            Integer solicitudId,
            Tipo tipo,
            String descripcion,
            String nombreArchivo,
            String rutaArchivo,
            int tamanoBytes,
            OffsetDateTime fechaCarga) {
        this.solicitudId = solicitudId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.tamanoBytes = tamanoBytes;
        this.fechaCarga = fechaCarga;
    }

    public void enviar() {
        this.borrador = false;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSolicitudId() {
        return solicitudId;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
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

    public int getVersion() {
        return version;
    }

    public Integer getReemplazaAId() {
        return reemplazaAId;
    }

    public EstadoRevision getEstadoRevision() {
        return estadoRevision;
    }

    public EtapaRevision getEtapaRevision() {
        return etapaRevision;
    }

    public String getComentarioRevision() {
        return comentarioRevision;
    }

    public Usuario getRevisadoPor() {
        return revisadoPor;
    }

    public OffsetDateTime getFechaRevision() {
        return fechaRevision;
    }

    public boolean isEsRequeridoFaltante() {
        return esRequeridoFaltante;
    }

    public boolean isBorrador() {
        return borrador;
    }
}
