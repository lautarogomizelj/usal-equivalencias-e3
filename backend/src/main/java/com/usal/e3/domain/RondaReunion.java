package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Fila de `rondas_reunion` (contrato §2.10): una tanda de fechas que el Académico le propone al aspirante. Son
 * dos como máximo, y cada una es un estado distinto de la solicitud (RF-ASP-005).
 */
@Entity
@Table(name = "rondas_reunion")
public class RondaReunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private int numero;
    private Integer propuestaPorId;
    private OffsetDateTime fechaPropuesta;
    private String motivoOtrasFechas;
    private String disponibilidadAspirante;
    private boolean rechazada;

    protected RondaReunion() {}

    public RondaReunion(Integer solicitudId, int numero, Integer propuestaPorId, OffsetDateTime fechaPropuesta) {
        this.solicitudId = solicitudId;
        this.numero = numero;
        this.propuestaPorId = propuestaPorId;
        this.fechaPropuesta = fechaPropuesta;
    }

    /** El aspirante no puede en ninguna de las fechas de la 1ª ronda y dice cuándo sí. */
    public void pedirOtrasFechas(String motivo, String disponibilidad) {
        this.motivoOtrasFechas = motivo;
        this.disponibilidadAspirante = disponibilidad;
    }

    /** El aspirante rechaza la 2ª ronda: la fecha se pauta por chat. */
    public void rechazar() {
        this.rechazada = true;
    }

    public Integer getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public String getMotivoOtrasFechas() {
        return motivoOtrasFechas;
    }

    public String getDisponibilidadAspirante() {
        return disponibilidadAspirante;
    }
}
