package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;
    private String nombre;
    private String facultad;
    private String descripcion;
    private boolean activo = true;

    public Carrera() {}

    public Carrera(String codigo, String nombre, String facultad, String descripcion) {
        editar(codigo, nombre, facultad, descripcion);
    }

    /** RF-ADMIN-002: el Admin cambia los datos; la baja es lógica, con `activo`. */
    public void editar(String codigo, String nombre, String facultad, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.facultad = facultad;
        this.descripcion = descripcion;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFacultad() {
        return facultad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }
}
