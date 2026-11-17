package com.usal.e3.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/** Archivos del trámite en disco. La ruta es relativa a `e3.archivos.directorio` y nunca sale de la API. */
@Service
public class ArchivoService {

    private final Path raiz;

    public ArchivoService(@Value("${e3.archivos.directorio}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
    }

    /** Guarda con un nombre propio dentro de `carpeta`; el nombre original solo se muestra. */
    public String guardar(String carpeta, byte[] contenido, String extension) {
        String ruta = carpeta + "/" + UUID.randomUUID() + "." + extension;
        Path destino = raiz.resolve(ruta);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return ruta;
    }

    public Resource leer(String ruta) {
        return new FileSystemResource(raiz.resolve(ruta));
    }

    public void borrar(String ruta) {
        try {
            Files.deleteIfExists(raiz.resolve(ruta));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
