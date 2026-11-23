package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.LecturaChat;
import com.usal.e3.domain.MensajeChat;
import com.usal.e3.domain.MensajeChat.TipoConsulta;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.LecturaChatRepository;
import com.usal.e3.repository.MensajeChatRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.repository.UsuarioRepository;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AnalisisService.Adjunto;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chat de la solicitud (RF-MSG-001 a 004). Escriben el aspirante y los responsables; el resto del personal
 * solo lee. El historial es de la solicitud: si cambia el responsable, el nuevo lo ve completo.
 */
@Service
public class ChatService {

    private static final int MAX_CONTENIDO = 500;
    private static final long MAX_ADJUNTO = 5L * 1024 * 1024;

    /** Un mensaje como lo ve quien pregunta (contrato §8.1). */
    public record Mensaje(MensajeChat mensaje, Cuenta autor, boolean visto, boolean esMio) {}

    private final SolicitudRepository solicitudes;
    private final MensajeChatRepository mensajes;
    private final LecturaChatRepository lecturas;
    private final UsuarioRepository usuarios;
    private final CuentaService cuentas;
    private final ArchivoService archivos;
    private final TransicionService transiciones;
    private final CatalogoService catalogo;

    public ChatService(
            SolicitudRepository solicitudes,
            MensajeChatRepository mensajes,
            LecturaChatRepository lecturas,
            UsuarioRepository usuarios,
            CuentaService cuentas,
            ArchivoService archivos,
            TransicionService transiciones,
            CatalogoService catalogo) {
        this.solicitudes = solicitudes;
        this.mensajes = mensajes;
        this.lecturas = lecturas;
        this.usuarios = usuarios;
        this.cuentas = cuentas;
        this.archivos = archivos;
        this.transiciones = transiciones;
        this.catalogo = catalogo;
    }

    /** Del más viejo al más nuevo. `despuesDe` trae solo los que llegaron después de ese mensaje. */
    @Transactional(readOnly = true)
    public Page<Mensaje> mensajes(SesionActual actual, Integer solicitudId, int despuesDe, Pageable pagina) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = visible(cuenta, solicitudId);
        Page<MensajeChat> tanda =
                mensajes.findBySolicitudIdAndIdGreaterThanOrderByIdAsc(solicitudId, despuesDe, pagina);
        Map<Integer, Usuario> personal = usuarios
                .findAllById(tanda.getContent().stream()
                        .filter(mensaje -> mensaje.getAutorTipo() == TitularTipo.USUARIO)
                        .map(MensajeChat::getAutorId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));
        List<LecturaChat> leidoHasta = lecturas.findBySolicitudId(solicitudId);
        return tanda.map(mensaje -> comoLoVe(
                cuenta,
                mensaje,
                mensaje.getAutorTipo() == TitularTipo.USUARIO
                        ? personal.get(mensaje.getAutorId())
                        : solicitud.getAspirante(),
                leidoHasta));
    }

    /** El mensaje lleva texto, adjunto o las dos cosas (RF-MSG-002). */
    @Transactional
    public Mensaje enviar(
            SesionActual actual, Integer solicitudId, String contenido, String tipoConsulta, Adjunto adjunto) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = visible(cuenta, solicitudId);
        exigirQueSeaDelChat(cuenta, solicitud);
        if (!hayResponsable(solicitud)) {
            throw new ApiException(ErrorCode.CHAT_DESHABILITADO);
        }
        if (estado(solicitud).isEsTerminal()) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        String texto = contenido == null ? "" : contenido.strip();
        if (texto.isEmpty() && adjunto == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "contenido", "Escribí un mensaje o adjuntá un archivo.");
        }
        if (texto.length() > MAX_CONTENIDO) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "contenido", "Máximo " + MAX_CONTENIDO + " caracteres.");
        }
        TipoConsulta tipo = Arrays.stream(TipoConsulta.values())
                .filter(valor -> valor.name().equals(tipoConsulta))
                .findFirst()
                .orElseThrow(() ->
                        ApiException.enCampo(ErrorCode.VALIDACION, "tipoConsulta", "Elegí un tipo de consulta."));
        String nombre = null;
        String ruta = null;
        if (adjunto != null) {
            String extension = extension(adjunto.contenido());
            if (adjunto.contenido().length > MAX_ADJUNTO) {
                throw new ApiException(ErrorCode.LIMITE_TAMANO, "El adjunto supera los 5 MB.");
            }
            ruta = archivos.guardar(DocumentoService.carpeta(solicitud) + "/chat", adjunto.contenido(), extension);
            archivos.borrarSiSeDeshace(ruta);
            nombre = DocumentoService.nombreVisible(adjunto.nombre(), extension);
        }
        MensajeChat mensaje = mensajes.save(
                new MensajeChat(solicitudId, cuenta, texto, tipo, nombre, ruta, OffsetDateTime.now()));
        return new Mensaje(mensaje, cuenta, false, true);
    }

    /** Solo cuentan las lecturas de quienes participan del chat: que un observador lo abra no lo marca como visto. */
    @Transactional
    public void marcarLeidos(SesionActual actual, Integer solicitudId, Integer hastaMensajeId) {
        Cuenta cuenta = cuentas.de(actual);
        // Con la solicitud bloqueada, dos pestañas del mismo usuario no crean dos filas de lectura.
        Solicitud solicitud = solicitudes.buscarParaActualizar(solicitudId)
                .filter(s -> s.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        exigirQueSeaDelChat(cuenta, solicitud);
        mensajes.findById(hastaMensajeId)
                .filter(mensaje -> mensaje.getSolicitudId().equals(solicitudId))
                .orElseThrow(() -> ApiException.enCampo(
                        ErrorCode.VALIDACION, "hastaMensajeId", "El mensaje no es de esta solicitud."));
        OffsetDateTime ahora = OffsetDateTime.now();
        lecturas.findBySolicitudId(solicitudId).stream()
                .filter(lectura -> lectura.esDe(cuenta.tipo(), cuenta.getId()))
                .findFirst()
                .orElseGet(() -> lecturas.save(new LecturaChat(solicitudId, cuenta, ahora)))
                .avanzarHasta(hastaMensajeId, ahora);
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga adjunto(SesionActual actual, Integer solicitudId, Integer mensajeId) {
        visible(cuentas.de(actual), solicitudId);
        MensajeChat mensaje = mensajes.findById(mensajeId)
                .filter(m -> m.getSolicitudId().equals(solicitudId) && m.getAdjuntoRuta() != null)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        String ruta = mensaje.getAdjuntoRuta();
        String contentType =
                ruta.endsWith(".pdf") ? "application/pdf" : ruta.endsWith(".png") ? "image/png" : "image/jpeg";
        return new ArchivoDescarga(archivos.leer(ruta), mensaje.getAdjuntoNombre(), contentType);
    }

    /** Mensajes de otros que todavía no leyó. Quien no participa del chat no tiene pendientes. */
    int noLeidos(Cuenta cuenta, Solicitud solicitud) {
        if (!esDelChat(cuenta, solicitud)) {
            return 0;
        }
        int leidoHasta = lecturas.findBySolicitudId(solicitud.getId()).stream()
                .filter(lectura -> lectura.esDe(cuenta.tipo(), cuenta.getId()))
                .map(LecturaChat::getHastaMensajeId)
                .findFirst()
                .orElse(0);
        return (int) mensajes.findBySolicitudIdAndIdGreaterThan(solicitud.getId(), leidoHasta).stream()
                .filter(mensaje -> !mensaje.esDe(cuenta.tipo(), cuenta.getId()))
                .count();
    }

    /** Con la solicitud finalizada el chat queda en solo lectura (interfaces §4.7.2). */
    boolean puedeEscribir(Cuenta cuenta, Solicitud solicitud, Estado estado) {
        return esDelChat(cuenta, solicitud) && hayResponsable(solicitud) && !estado.isEsTerminal();
    }

    /** El aspirante dueño y los responsables académico y administrativo (RF-MSG-001). */
    private static boolean esDelChat(Cuenta cuenta, Solicitud solicitud) {
        return solicitud.esPropiaDe(cuenta) || solicitud.tieneComoResponsable(cuenta);
    }

    private static boolean hayResponsable(Solicitud solicitud) {
        return solicitud.getResponsableAcademico() != null || solicitud.getResponsableAdministrativo() != null;
    }

    /** 403 `NO_PARTICIPANTE` para el observador y `NO_RESPONSABLE` para el participante que no tomó el proceso. */
    private void exigirQueSeaDelChat(Cuenta cuenta, Solicitud solicitud) {
        if (!esDelChat(cuenta, solicitud)) {
            transiciones.exigirQuePuedaActuar(cuenta, solicitud);
            throw new ApiException(ErrorCode.ACCESO_DENEGADO);
        }
    }

    private static Mensaje comoLoVe(Cuenta cuenta, MensajeChat mensaje, Cuenta autor, List<LecturaChat> leidoHasta) {
        boolean visto = leidoHasta.stream()
                .anyMatch(lectura -> !lectura.esDe(mensaje.getAutorTipo(), mensaje.getAutorId())
                        && lectura.getHastaMensajeId() >= mensaje.getId());
        return new Mensaje(mensaje, autor, visto, mensaje.esDe(cuenta.tipo(), cuenta.getId()));
    }

    private Estado estado(Solicitud solicitud) {
        return catalogo.estadosPorCodigo().get(solicitud.getEstadoCodigo());
    }

    private Solicitud visible(Cuenta cuenta, Integer solicitudId) {
        return solicitudes.findById(solicitudId)
                .filter(solicitud -> solicitud.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    /** Los formatos de la documentación más PNG, por el contenido y no por el nombre (interfaces, Anexo A #16). */
    private static String extension(byte[] contenido) {
        boolean png = contenido.length >= 4
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 'P'
                && contenido[2] == 'N'
                && contenido[3] == 'G';
        if (png) {
            return "png";
        }
        try {
            return DocumentoService.extension(contenido);
        } catch (ApiException ex) {
            throw new ApiException(
                    ErrorCode.ARCHIVO_INVALIDO, "Formato no permitido. Solo se aceptan archivos PDF, JPG o PNG.");
        }
    }
}
