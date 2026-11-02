package com.usal.e3.security;

import com.usal.e3.domain.TitularTipo;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Quién hace el pedido. Es el principal de Spring Security. */
public record SesionActual(
        UUID sesionId, TitularTipo tipo, Integer id, String email, String rol, OffsetDateTime expiraEn) {}
