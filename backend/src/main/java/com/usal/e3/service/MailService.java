package com.usal.e3.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Los mails son solo avisos: el trámite no depende de ellos (RF-ESTADO-002). */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public MailService(JavaMailSender mailSender, @Value("${e3.mail-remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCodigoValidacion(String email, String codigo) {
        enviar(email, "E3: tu código de validación", "Tu código de validación es " + codigo + ". Vence en 10 minutos.");
    }

    public void enviarCodigoRecupero(String email, String codigo) {
        enviar(email, "E3: recuperá tu contraseña", "Tu código de recupero es " + codigo + ". Vence en 10 minutos.");
    }

    /**
     * Aviso de un cambio en el trámite. Sale recién cuando la transacción se confirma: si la acción se deshace
     * no se avisa algo que no pasó. Si el mail falla se registra y la acción queda hecha.
     */
    public void enviarAviso(String destinatario, String asunto, String texto) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            enviarSinFrenar(destinatario, asunto, texto);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enviarSinFrenar(destinatario, asunto, texto);
            }
        });
    }

    private void enviarSinFrenar(String destinatario, String asunto, String texto) {
        try {
            enviar(destinatario, asunto, texto);
        } catch (MailException ex) {
            log.warn("No se pudo enviar el aviso \"{}\" a {}", asunto, destinatario, ex);
        }
    }

    private void enviar(String destinatario, String asunto, String texto) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        mailSender.send(mensaje);
    }
}
