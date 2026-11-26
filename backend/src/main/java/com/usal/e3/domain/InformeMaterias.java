package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Fila de `informes_materias` (contrato §2.13): lo que el Académico informa al aceptar los documentos
 * legalizados (RF-ACAD-005) y el aspirante ve desde `CUPON-SOL` (RF-ASP-006).
 */
@Entity
@Table(name = "informes_materias")
public class InformeMaterias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private int materiasReconocidas;
    private String observaciones;
    private Integer cargadoPorId;
    private OffsetDateTime fechaCarga;

    protected InformeMaterias() {}

    public InformeMaterias(
            Integer solicitudId, int materiasReconocidas, String observaciones, Usuario cargadoPor, OffsetDateTime fecha) {
        this.solicitudId = solicitudId;
        this.materiasReconocidas = materiasReconocidas;
        this.observaciones = observaciones;
        this.cargadoPorId = cargadoPor.getId();
        this.fechaCarga = fecha;
    }

    public int getMateriasReconocidas() {
        return materiasReconocidas;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
