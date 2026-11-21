package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `reuniones` (contrato §2.12): la reunión pactada. La vigente de una solicitud es la última. */
@Entity
@Table(name = "reuniones")
public class Reunion {

    public enum Origen {
        ELEGIDA_ASPIRANTE,
        FIJADA_CHAT
    }

    public enum Resultado {
        PENDIENTE,
        REALIZADA,
        NO_ASISTIO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;
    private OffsetDateTime inicio;
    private OffsetDateTime fin;

    @Enumerated(EnumType.STRING)
    private Origen origen;

    private Integer horarioPropuestoId;
    private String enlaceMeet;

    @Enumerated(EnumType.STRING)
    private Resultado resultado = Resultado.PENDIENTE;

    protected Reunion() {}

    /** El aspirante eligió uno de los horarios propuestos (RF-ASP-005). */
    public Reunion(Integer solicitudId, HorarioPropuesto horario) {
        this.solicitudId = solicitudId;
        this.inicio = horario.getInicio();
        this.fin = horario.getFin();
        this.origen = Origen.ELEGIDA_ASPIRANTE;
        this.horarioPropuestoId = horario.getId();
    }

    public void cargarEnlace(String enlaceMeet) {
        this.enlaceMeet = enlaceMeet;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public OffsetDateTime getFin() {
        return fin;
    }

    public Origen getOrigen() {
        return origen;
    }

    public String getEnlaceMeet() {
        return enlaceMeet;
    }

    public Resultado getResultado() {
        return resultado;
    }
}
