package com.usal.e3.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Solicitud de equivalencias (RF-ASP-001, contrato §2.6). El estado solo lo cambia el motor de transiciones. */
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigoExp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aspirante_id")
    private Aspirante aspirante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrera_destino_id")
    private Carrera carreraDestino;

    private String estadoCodigo;

    @ManyToOne
    @JoinColumn(name = "responsable_academico_id")
    private Usuario responsableAcademico;

    @ManyToOne
    @JoinColumn(name = "responsable_administrativo_id")
    private Usuario responsableAdministrativo;

    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaUltimaMod;
    private OffsetDateTime fechaUltimoCambioEstado;
    private OffsetDateTime fechaAsignacionAcademico;
    private OffsetDateTime fechaAsignacionAdministrativo;

    @Embedded
    private DatosOrigen datosOrigen;

    protected Solicitud() {}

    /** Nace en `SOL-INI` y sin responsable (RF-ASP-001). */
    public Solicitud(String codigoExp, Aspirante aspirante, Carrera carreraDestino, DatosOrigen datosOrigen) {
        OffsetDateTime ahora = OffsetDateTime.now();
        this.codigoExp = codigoExp;
        this.aspirante = aspirante;
        this.carreraDestino = carreraDestino;
        this.datosOrigen = datosOrigen;
        this.estadoCodigo = Estado.SOL_INI;
        this.fechaCreacion = ahora;
        this.fechaUltimaMod = ahora;
        this.fechaUltimoCambioEstado = ahora;
    }

    public void cambiarEstado(String estadoCodigo, OffsetDateTime fecha) {
        this.estadoCodigo = estadoCodigo;
        this.fechaUltimoCambioEstado = fecha;
        this.fechaUltimaMod = fecha;
    }

    public void modificada(OffsetDateTime fecha) {
        this.fechaUltimaMod = fecha;
    }

    /** El personal ve todas (acceso básico, RF-AUTH-005); el aspirante, solo las suyas. */
    public boolean visiblePara(Cuenta cuenta) {
        return cuenta.tipo() == TitularTipo.USUARIO || esPropiaDe(cuenta);
    }

    public boolean esPropiaDe(Cuenta cuenta) {
        return cuenta.tipo() == TitularTipo.ASPIRANTE && aspirante.getId().equals(cuenta.getId());
    }

    public Integer getId() {
        return id;
    }

    public String getCodigoExp() {
        return codigoExp;
    }

    public Aspirante getAspirante() {
        return aspirante;
    }

    public Carrera getCarreraDestino() {
        return carreraDestino;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public Usuario getResponsableAcademico() {
        return responsableAcademico;
    }

    public Usuario getResponsableAdministrativo() {
        return responsableAdministrativo;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaUltimaMod() {
        return fechaUltimaMod;
    }

    public OffsetDateTime getFechaUltimoCambioEstado() {
        return fechaUltimoCambioEstado;
    }

    public OffsetDateTime getFechaAsignacionAcademico() {
        return fechaAsignacionAcademico;
    }

    public OffsetDateTime getFechaAsignacionAdministrativo() {
        return fechaAsignacionAdministrativo;
    }

    public DatosOrigen getDatosOrigen() {
        return datosOrigen;
    }
}
