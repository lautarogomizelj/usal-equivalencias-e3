package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Los avisos del trámite salen después del commit y nunca frenan la acción (RF-ESTADO-002). */
class MailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailService mail = new MailService(mailSender, "no-responder@usal.edu.ar");

    @AfterEach
    void cerrarTransaccion() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void dentroDeUnaTransaccionElAvisoSaleRecienAlConfirmar() {
        TransactionSynchronizationManager.initSynchronization();

        mail.enviarAviso("a@b.com", "Asunto", "Texto");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void siLaTransaccionSeDeshaceElAvisoNoSale() {
        TransactionSynchronizationManager.initSynchronization();

        mail.enviarAviso("a@b.com", "Asunto", "Texto");
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(pendiente -> pendiente.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void siElMailFallaLaAccionSigue() {
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));
        TransactionSynchronizationManager.initSynchronization();

        mail.enviarAviso("a@b.com", "Asunto", "Texto");

        assertThatCode(() -> TransactionSynchronizationManager.getSynchronizations()
                        .forEach(TransactionSynchronization::afterCommit))
                .doesNotThrowAnyException();
    }

    @Test
    void sinTransaccionElAvisoSaleEnElMomento() {
        mail.enviarAviso("a@b.com", "Asunto", "Texto");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
