package com.usal.e3.service;

import java.time.Duration;

/** Login exitoso: el token va en la cookie y los datos en el cuerpo de la respuesta. */
public record SesionIniciada(String token, Duration duracion, DatosSesion datos) {}
