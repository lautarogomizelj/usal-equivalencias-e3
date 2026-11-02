package com.usal.e3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Los mails son solo avisos: el trámite no depende de ellos (RF-ESTADO-002). */
@Service
public class MailService {

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

    private void enviar(String destinatario, String asunto, String texto) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        mailSender.send(mensaje);
    }
}
