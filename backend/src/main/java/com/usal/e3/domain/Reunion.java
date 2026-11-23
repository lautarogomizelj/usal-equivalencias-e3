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
    private String notas;

    @Enumerated(EnumType.STRING)
    private Resultado resultado = Resultado.PENDIENTE;

    private OffsetDateTime fechaResultado;

    protected Reunion() {}

    /** El Académico fijó la fecha que pautaron por chat (RF-ACAD-003). Dura 1 hora, como las propuestas. */
    public Reunion(Integer solicitudId, OffsetDateTime inicio) {
        this.solicitudId = solicitudId;
        this.inicio = inicio;
        this.fin = inicio.plusHours(1);
        this.origen = Origen.FIJADA_CHAT;
    }

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

    /** RF-ACAD-004: la reunión se hizo, con notas opcionales del Académico, o el aspirante no se presentó. */
    public void cerrar(Resultado resultado, String notas, OffsetDateTime fecha) {
        this.resultado = resultado;
        this.notas = notas;
        this.fechaResultado = fecha;
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

    public String getNotas() {
        return notas;
    }

    public Resultado getResultado() {
        return resultado;
    }
}
