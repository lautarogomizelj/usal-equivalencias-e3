package com.usal.e3.service;

import org.springframework.core.io.Resource;

public record ArchivoDescarga(Resource recurso, String nombre, String contentType) {}
