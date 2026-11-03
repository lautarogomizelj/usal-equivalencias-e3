package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Código de 6 dígitos de validación de email o recupero de contraseña (contrato §2.19). */
@Entity
@Table(name = "codigos_verificacion")
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer aspiranteId;

    @Enumerated(EnumType.STRING)
    private PropositoCodigo proposito;

    private String codigoHash;
    private OffsetDateTime expiraEn;
    private int intentos;
    private OffsetDateTime usadoEn;

    protected CodigoVerificacion() {}

    public CodigoVerificacion(
            Integer aspiranteId, PropositoCodigo proposito, String codigoHash, OffsetDateTime expiraEn, int intentos) {
        this.aspiranteId = aspiranteId;
        this.proposito = proposito;
        this.codigoHash = codigoHash;
        this.expiraEn = expiraEn;
        this.intentos = intentos;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public OffsetDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(OffsetDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public void setUsadoEn(OffsetDateTime usadoEn) {
        this.usadoEn = usadoEn;
    }
}
