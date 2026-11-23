package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Fila de `mensajes_chat` (RF-MSG-002, contrato §2.16). El chat es de la solicitud, no de quien escribe. */
@Entity
@Table(name = "mensajes_chat")
public class MensajeChat {

    public enum TipoConsulta {
        DOCUMENTACION,
        ESTADO,
        CALCULOS,
        FECHAS,
        OTRO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer solicitudId;

    @Enumerated(EnumType.STRING)
    private TitularTipo autorTipo;

    private Integer autorId;
    private String contenido;

    @Enumerated(EnumType.STRING)
    private TipoConsulta tipoConsulta;

    private String adjuntoNombre;
    private String adjuntoRuta;
    private OffsetDateTime fechaEnvio;

    protected MensajeChat() {}

    public MensajeChat(
            Integer solicitudId,
            Cuenta autor,
            String contenido,
            TipoConsulta tipoConsulta,
            String adjuntoNombre,
            String adjuntoRuta,
            OffsetDateTime fechaEnvio) {
        this.solicitudId = solicitudId;
        this.autorTipo = autor.tipo();
        this.autorId = autor.getId();
        this.contenido = contenido;
        this.tipoConsulta = tipoConsulta;
        this.adjuntoNombre = adjuntoNombre;
        this.adjuntoRuta = adjuntoRuta;
        this.fechaEnvio = fechaEnvio;
    }

    public boolean esDe(TitularTipo tipo, Integer id) {
        return autorTipo == tipo && autorId.equals(id);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSolicitudId() {
        return solicitudId;
    }

    public TitularTipo getAutorTipo() {
        return autorTipo;
    }

    public Integer getAutorId() {
        return autorId;
    }

    public String getContenido() {
        return contenido;
    }

    public TipoConsulta getTipoConsulta() {
        return tipoConsulta;
    }

    public String getAdjuntoNombre() {
        return adjuntoNombre;
    }

    public String getAdjuntoRuta() {
        return adjuntoRuta;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
