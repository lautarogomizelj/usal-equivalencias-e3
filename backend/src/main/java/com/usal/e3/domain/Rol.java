package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {

    public static final String ACADEMICO = "ACAD";
    public static final String SECRETARIA = "SECRE";
    public static final String ADMIN = "ADMIN";
    public static final String REVISOR = "REVISOR";
    public static final String ASPIRANTE = "ASPIRANTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;

    public String getCodigo() {
        return codigo;
    }
}
