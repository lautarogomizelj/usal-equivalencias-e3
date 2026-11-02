package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Personal @usal y Admin General (Requisitos v12 §5, contrato §2.2). */
@Entity
@Table(name = "usuarios")
public class Usuario implements Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    private boolean activo = true;
    private int intentosLoginFallidos;
    private OffsetDateTime bloqueadoHasta;
    private OffsetDateTime fechaUltimoIntentoFallido;
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();
    private OffsetDateTime fechaUltimoLogin;
    private String telefono;
    private String avatar;
    private String googleSub;
    private String passwordHash;

    protected Usuario() {}

    public Usuario(String email, String nombre, Rol rol) {
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }

    @Override
    public TitularTipo tipo() {
        return TitularTipo.USUARIO;
    }

    @Override
    public String rolCodigo() {
        return rol.getCodigo();
    }

    @Override
    public boolean esOAuth() {
        return !Rol.ADMIN.equals(rolCodigo());
    }

    @Override
    public boolean isEmailValidado() {
        return true;
    }

    @Override
    public String getDni() {
        return null;
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
    public String getNombre() {
        return nombre;
    }

    @Override
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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
    public void setFechaUltimoLogin(OffsetDateTime fecha) {
        this.fechaUltimoLogin = fecha;
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
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
