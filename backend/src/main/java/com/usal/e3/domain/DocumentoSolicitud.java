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

    /** Documento requerido que el aspirante no cargó: sin archivo y marcado para recargar (RF-ACAD-009). */
    public static DocumentoSolicitud faltante(
            Integer solicitudId, Tipo tipo, String descripcion, Usuario revisor, OffsetDateTime fecha) {
        DocumentoSolicitud faltante = new DocumentoSolicitud();
        faltante.solicitudId = solicitudId;
        faltante.tipo = tipo;
        faltante.descripcion = descripcion;
        faltante.esRequeridoFaltante = true;
        faltante.borrador = false;
        faltante.marcar(EstadoRevision.RECARGAR, null, revisor, fecha);
        return faltante;
    }

    public void enviar() {
        this.borrador = false;
    }

    /** Nueva versión del documento marcado para recargar; la anterior queda archivada (RF-ASP-004). */
    public void reemplazar(DocumentoSolicitud anterior) {
        this.reemplazaAId = anterior.id;
        this.version = anterior.tieneArchivo() ? anterior.version + 1 : anterior.version;
        this.etapaRevision = anterior.etapaRevision;
    }

    public void marcar(EstadoRevision estado, String comentario, Usuario revisor, OffsetDateTime fecha) {
        this.estadoRevision = estado;
        this.comentarioRevision = comentario;
        this.revisadoPor = revisor;
        this.fechaRevision = fecha;
    }

    public boolean tieneArchivo() {
        return rutaArchivo != null;
    }

    /** Cómo se nombra el documento en los mails y en los errores. */
    public String titulo() {
        String nombre = switch (tipo) {
            case ANALITICO -> "Certificado analítico";
            case PLAN -> "Plan de estudios";
            case PROGRAMA -> "Programa de la asignatura";
            case OTRO -> "Otro documento";
        };
        return descripcion == null ? nombre : nombre + ": " + descripcion;
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
