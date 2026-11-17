package com.usal.e3.service;

/** Una fila de la tabla de transiciones de RF-ESTADO-001. */
public record Transicion(String accion, String rolActor, String origen, String destino) {}
