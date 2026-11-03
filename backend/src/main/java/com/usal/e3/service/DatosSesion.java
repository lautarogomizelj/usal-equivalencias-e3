package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import java.time.OffsetDateTime;
import java.util.List;

/** Lo que el contrato llama `Sesion` (§4.1). `carreras` es nulo si el rol no trabaja por carrera. */
public record DatosSesion(Cuenta cuenta, List<Carrera> carreras, OffsetDateTime expiraEn) {}
