package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.EventoAuditoria;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.repository.AuditoriaRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Traza de RF-AUDIT-001. Corre en la transacción de quien la llama. */
@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoria;

    public AuditoriaService(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    /** La propia cuenta actúa sobre sí misma: login, cambio de contraseña, perfil… */
    public void registrar(Cuenta cuenta, String accion, Map<String, Object> detalles, String ip) {
        registrar(cuenta, accion, cuenta, detalles, ip);
    }

    public void registrar(Cuenta actor, String accion, Cuenta objetivo, Map<String, Object> detalles, String ip) {
        auditoria.save(new EventoAuditoria(
                usuarioId(actor),
                actor.getEmail(),
                accion,
                objetivo.tipo().name().toLowerCase(),
                objetivo.getId(),
                detalles,
                ip));
    }

    /** Siempre por código, nunca por nombre de estado (RF-ESTADO-004). */
    public void registrarCambioEstado(
            Cuenta actor, Integer solicitudId, String anterior, String nuevo, String comentario, String ip) {
        auditoria.save(EventoAuditoria.cambioEstado(
                usuarioId(actor), actor.getEmail(), solicitudId, anterior, nuevo, comentario, ip));
    }

    private static Integer usuarioId(Cuenta actor) {
        return actor.tipo() == TitularTipo.USUARIO ? actor.getId() : null;
    }
}
