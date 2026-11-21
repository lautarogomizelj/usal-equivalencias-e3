package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `horarios_propuestos` (contrato §2.11): una franja de 1 hora dentro de una ronda. */
@Entity
@Table(name = "horarios_propuestos")
public class HorarioPropuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer rondaId;
    private OffsetDateTime inicio;
    private OffsetDateTime fin;
    private boolean elegido;

    protected HorarioPropuesto() {}

    public HorarioPropuesto(Integer rondaId, OffsetDateTime inicio) {
        this.rondaId = rondaId;
        this.inicio = inicio;
        this.fin = inicio.plusHours(1);
    }

    public void elegir() {
        this.elegido = true;
    }

    public Integer getId() {
        return id;
    }

    public Integer getRondaId() {
        return rondaId;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public OffsetDateTime getFin() {
        return fin;
    }

    public boolean isElegido() {
        return elegido;
    }
}
