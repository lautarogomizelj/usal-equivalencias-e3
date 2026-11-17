package com.usal.e3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Formulario inicial de RF-ASP-001: lo que la cuenta del aspirante no trae (interfaces, Anexo A #1). */
@Embeddable
public class DatosOrigen {

    @Column(name = "universidad_origen")
    private String universidad;

    @Column(name = "facultad_origen")
    private String facultad;

    @Column(name = "carrera_origen")
    private String carrera;

    private String condicionAcademica;
    private String observaciones;

    protected DatosOrigen() {}

    public DatosOrigen(
            String universidad, String facultad, String carrera, String condicionAcademica, String observaciones) {
        this.universidad = universidad;
        this.facultad = facultad;
        this.carrera = carrera;
        this.condicionAcademica = condicionAcademica;
        this.observaciones = observaciones;
    }

    public String getUniversidad() {
        return universidad;
    }

    public String getFacultad() {
        return facultad;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getCondicionAcademica() {
        return condicionAcademica;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
