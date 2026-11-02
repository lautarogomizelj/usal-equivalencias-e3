package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Alimenta "Historial de accesos" y "Sesiones activas" (RF-AUTH-007, contrato §2.20). */
@Entity
@Table(name = "sesiones")
public class Sesion {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TitularTipo titularTipo;

    private Integer titularId;
    private String ip;
    private String userAgent;
    private OffsetDateTime inicio;
    private OffsetDateTime ultimoUso;
    private OffsetDateTime expiraEn;
    private OffsetDateTime cerradaEn;

    protected Sesion() {}

    public Sesion(
            UUID id,
            TitularTipo titularTipo,
            Integer titularId,
            String ip,
            String userAgent,
            OffsetDateTime inicio,
            OffsetDateTime expiraEn) {
        this.id = id;
        this.titularTipo = titularTipo;
        this.titularId = titularId;
        this.ip = ip;
        this.userAgent = userAgent;
        this.inicio = inicio;
        this.ultimoUso = inicio;
        this.expiraEn = expiraEn;
    }

    public UUID getId() {
        return id;
    }

    public TitularTipo getTitularTipo() {
        return titularTipo;
    }

    public Integer getTitularId() {
        return titularId;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public OffsetDateTime getUltimoUso() {
        return ultimoUso;
    }

    public void setUltimoUso(OffsetDateTime ultimoUso) {
        this.ultimoUso = ultimoUso;
    }

    public OffsetDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(OffsetDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public OffsetDateTime getCerradaEn() {
        return cerradaEn;
    }

    public void setCerradaEn(OffsetDateTime cerradaEn) {
        this.cerradaEn = cerradaEn;
    }
}
