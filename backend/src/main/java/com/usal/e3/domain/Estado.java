package com.usal.e3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;

/** Catálogo de los 21 estados (RF-ESTADO-001). El resto del sistema los referencia por código. */
@Entity
@Table(name = "estados")
public class Estado {

    public static final String SOL_INI = "SOL-INI";
    public static final String DOC_REV = "DOC-REV";
    public static final String SOL_CANC = "SOL-CANC";

    /** Estados de tipo A para el aspirante: "Requieren tu acción" (interfaces §4.2). */
    public static final Set<String> ESPERAN_AL_ASPIRANTE = Set.of(
            "SOL-INI",
            "DOC-RECH",
            "REU-FEC-ASP-1",
            "REU-FEC-ASP-2",
            "REU-CHAT",
            "INSC-ACEP",
            "LEG-PEND",
            "LEG-RECH",
            "CUPON-PAG",
            "PAG-REENV");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean esTerminal;
    private boolean activo;

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsTerminal() {
        return esTerminal;
    }

    public boolean isActivo() {
        return activo;
    }
}
