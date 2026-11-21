package com.usal.e3.domain;

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

/**
 * Fila de `analisis_academico` (RF-ACAD-002, contrato §2.9): la matriz de equivalencias o el horario de cursada
 * que carga el Académico, como archivo, como notas o las dos cosas. Es versionada y la API expone solo la vigente.
 */
@Entity
@Table(name = "analisis_academico")
public class AnalisisAcademico {

    public enum Tipo {
        MATRIZ_EQUIVALENCIAS,
        HORARIO_CURSADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private int version = 1;
    private String nombreArchivo;
    private String rutaArchivo;
    private String notas;
    private OffsetDateTime fechaCarga;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subido_por_id")
    private Usuario subidoPor;

    protected AnalisisAcademico() {}

    public AnalisisAcademico(
            Integer solicitudId,
            Tipo tipo,
            String nombreArchivo,
            String rutaArchivo,
            String notas,
            Usuario subidoPor,
            OffsetDateTime fechaCarga) {
        this.solicitudId = solicitudId;
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.notas = notas;
        this.subidoPor = subidoPor;
        this.fechaCarga = fechaCarga;
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

    public int getVersion() {
        return version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public String getNotas() {
        return notas;
    }

    public OffsetDateTime getFechaCarga() {
        return fechaCarga;
    }

    public Usuario getSubidoPor() {
        return subidoPor;
    }
}
