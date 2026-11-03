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
                actor.tipo() == TitularTipo.USUARIO ? actor.getId() : null,
                actor.getEmail(),
                accion,
                objetivo.tipo().name().toLowerCase(),
                objetivo.getId(),
                detalles,
                ip));
    }
}
