package com.usal.e3.api.chat;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.MensajeChat;
import com.usal.e3.service.ChatService.Mensaje;
import java.time.OffsetDateTime;

/** `Mensaje` del contrato (§8.1). */
public record MensajeResponse(
        Integer id,
        Autor autor,
        String contenido,
        String tipoConsulta,
        Adjunto adjunto,
        OffsetDateTime fechaEnvio,
        boolean visto,
        boolean esMio) {

    /** `rol` es el código del rol de quien escribió: sirve para rotularlo aunque ya no sea el responsable. */
    public record Autor(String tipo, Integer id, String nombre, String rol, String avatar) {}

    public record Adjunto(String nombre, String urlDescarga) {}

    static MensajeResponse de(Mensaje vista) {
        MensajeChat mensaje = vista.mensaje();
        Cuenta autor = vista.autor();
        return new MensajeResponse(
                mensaje.getId(),
                new Autor(autor.tipo().name(), autor.getId(), autor.nombreCompleto(), autor.rolCodigo(), autor.getAvatar()),
                mensaje.getContenido(),
                mensaje.getTipoConsulta().name(),
                mensaje.getAdjuntoRuta() == null
                        ? null
                        : new Adjunto(
                                mensaje.getAdjuntoNombre(),
                                "/solicitudes/" + mensaje.getSolicitudId() + "/chat/mensajes/" + mensaje.getId()
                                        + "/adjunto"),
                mensaje.getFechaEnvio(),
                vista.visto(),
                vista.esMio());
    }
}
