package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Cuenta externa del aspirante (Requisitos v12 §5, contrato §2.3). */
@Entity
@Table(name = "aspirantes")
public class Aspirante implements Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String passwordHash;
    private String nombre;
    private String telefono;
    private String dni;
    private boolean emailValidado;
    private int intentosLoginFallidos;
    private OffsetDateTime bloqueadoHasta;
    private OffsetDateTime fechaUltimoIntentoFallido;
    private boolean activo = true;
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();
    private OffsetDateTime fechaUltimoLogin;
    private String avatar;

    protected Aspirante() {}

    public Aspirante(String email, String passwordHash, String nombre, String telefono, String dni) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.telefono = telefono;
        this.dni = dni;
    }

    @Override
    public TitularTipo tipo() {
        return TitularTipo.ASPIRANTE;
    }

    @Override
    public String rolCodigo() {
        return Rol.ASPIRANTE;
    }

    @Override
    public boolean esOAuth() {
        return false;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String getTelefono() {
        return telefono;
    }

    @Override
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String getDni() {
        return dni;
    }

    @Override
    public boolean isEmailValidado() {
        return emailValidado;
    }

    public void setEmailValidado(boolean emailValidado) {
        this.emailValidado = emailValidado;
    }

    @Override
    public int getIntentosLoginFallidos() {
        return intentosLoginFallidos;
    }

    @Override
    public void setIntentosLoginFallidos(int intentos) {
        this.intentosLoginFallidos = intentos;
    }

    @Override
    public OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    @Override
    public void setBloqueadoHasta(OffsetDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    @Override
    public OffsetDateTime getFechaUltimoIntentoFallido() {
        return fechaUltimoIntentoFallido;
    }

    @Override
    public void setFechaUltimoIntentoFallido(OffsetDateTime fecha) {
        this.fechaUltimoIntentoFallido = fecha;
    }

    @Override
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public void setFechaUltimoLogin(OffsetDateTime fecha) {
        this.fechaUltimoLogin = fecha;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
