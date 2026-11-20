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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    /** Dentro de una transacción. Se borra recién al confirmarla: un archivo borrado del disco no se recupera. */
    public void borrarAlConfirmar(String ruta) {
        borrarAlTerminar(ruta, TransactionSynchronization.STATUS_COMMITTED);
    }

    /** Dentro de una transacción, para un archivo recién guardado: si se deshace, no queda suelto en el disco. */
    public void borrarSiSeDeshace(String ruta) {
        borrarAlTerminar(ruta, TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    private void borrarAlTerminar(String ruta, int resultado) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == resultado) {
                    borrar(ruta);
                }
            }
        });
    }

    private void borrar(String ruta) {
        try {
            Files.deleteIfExists(raiz.resolve(ruta));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
