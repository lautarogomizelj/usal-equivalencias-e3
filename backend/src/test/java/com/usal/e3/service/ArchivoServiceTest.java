package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** El disco no vuelve atrás con la transacción: los archivos se borran cuando ya se sabe cómo terminó. */
class ArchivoServiceTest {

    @TempDir
    Path raiz;

    private ArchivoService archivos;
    private String ruta;

    @BeforeEach
    void guardarUnArchivo() {
        archivos = new ArchivoService(raiz.toString());
        ruta = archivos.guardar("2026/40123456/EXP-2026-001", "%PDF-1.7".getBytes(), "pdf");
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void cerrarTransaccion() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void elArchivoQuitadoSeBorraSoloSiLaTransaccionSeConfirma() {
        archivos.borrarAlConfirmar(ruta);
        assertThat(raiz.resolve(ruta)).exists();

        terminar(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(raiz.resolve(ruta)).exists();

        terminar(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(raiz.resolve(ruta)).doesNotExist();
    }

    @Test
    void elArchivoNuevoSeBorraSoloSiLaTransaccionSeDeshace() {
        archivos.borrarSiSeDeshace(ruta);

        terminar(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(raiz.resolve(ruta)).exists();

        terminar(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(raiz.resolve(ruta)).doesNotExist();
    }

    private static void terminar(int resultado) {
        TransactionSynchronizationManager.getSynchronizations().forEach(pendiente -> pendiente.afterCompletion(resultado));
    }
}
