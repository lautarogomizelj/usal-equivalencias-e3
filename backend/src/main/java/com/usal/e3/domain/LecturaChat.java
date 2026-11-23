package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `lecturas_chat`: hasta qué mensaje leyó un participante el chat de una solicitud. */
@Entity
@Table(name = "lecturas_chat")
public class LecturaChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;

    @Enumerated(EnumType.STRING)
    private TitularTipo lectorTipo;

    private Integer lectorId;
    private Integer hastaMensajeId;
    private OffsetDateTime fechaLectura;

    protected LecturaChat() {}

    public LecturaChat(Integer solicitudId, Cuenta lector, OffsetDateTime fecha) {
        this.solicitudId = solicitudId;
        this.lectorTipo = lector.tipo();
        this.lectorId = lector.getId();
        this.hastaMensajeId = 0;
        this.fechaLectura = fecha;
    }

    /** Solo avanza: marcar como leído un mensaje viejo no desmarca los que siguen. */
    public void avanzarHasta(Integer mensajeId, OffsetDateTime fecha) {
        if (mensajeId > hastaMensajeId) {
            this.hastaMensajeId = mensajeId;
            this.fechaLectura = fecha;
        }
    }

    public boolean esDe(TitularTipo tipo, Integer id) {
        return lectorTipo == tipo && lectorId.equals(id);
    }

    public Integer getHastaMensajeId() {
        return hastaMensajeId;
    }
}
