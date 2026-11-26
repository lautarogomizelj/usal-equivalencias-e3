package com.usal.e3.service;

import com.usal.e3.domain.AnalisisAcademico;
import com.usal.e3.domain.AnalisisAcademico.Tipo;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AnalisisRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Análisis preliminar (RF-ACAD-002): el Académico carga la matriz de equivalencias y el posible horario de
 * cursada y, en la misma acción, propone las fechas de la reunión. Después de la reunión puede actualizarlos
 * (RF-ACAD-004) y decide la inscripción (RF-ACAD-006).
 */
@Service
public class AnalisisService {

    public static final String ACTUALIZAR_ANALISIS = "ACTUALIZAR_ANALISIS";

    private static final int MAX_NOTAS = 2000;
    private static final int MAX_COMENTARIO = 500;

    /** Archivo que llegó en el pedido, con el nombre que traía. */
    public record Adjunto(String nombre, byte[] contenido) {}

    private final SolicitudRepository solicitudes;
    private final AnalisisRepository analisis;
    private final CuentaService cuentas;
    private final ArchivoService archivos;
    private final TransicionService transiciones;
    private final ReunionService reuniones;
    private final SolicitudService detalles;
    private final AuditoriaService auditoria;
    private final NotificacionService notificaciones;

    public AnalisisService(
            SolicitudRepository solicitudes,
            AnalisisRepository analisis,
            CuentaService cuentas,
            ArchivoService archivos,
            TransicionService transiciones,
            ReunionService reuniones,
            SolicitudService detalles,
            AuditoriaService auditoria,
            NotificacionService notificaciones) {
        this.solicitudes = solicitudes;
        this.analisis = analisis;
        this.cuentas = cuentas;
        this.archivos = archivos;
        this.transiciones = transiciones;
        this.reuniones = reuniones;
        this.detalles = detalles;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
    }

    /** La matriz y el horario vigentes: las versiones anteriores no salen por la API. */
    @Transactional(readOnly = true)
    public List<AnalisisAcademico> vigentes(SesionActual actual, Integer solicitudId) {
        return vigentes(visible(cuentas.de(actual), solicitudId));
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga archivo(SesionActual actual, Integer solicitudId, Integer analisisId) {
        AnalisisAcademico documento = vigentes(visible(cuentas.de(actual), solicitudId)).stream()
                .filter(a -> a.getId().equals(analisisId) && a.getRutaArchivo() != null)
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        String contentType = documento.getRutaArchivo().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        return new ArchivoDescarga(
                archivos.leer(documento.getRutaArchivo()), documento.getNombreArchivo(), contentType);
    }

    /** Transición 6: `ANAL-PEND` → `REU-FEC-ASP-1`. Todo junto o nada: no hay borradores. */
    @Transactional
    public DetalleSolicitud proponer(
            SesionActual actual,
            Integer id,
            String estadoEsperado,
            Adjunto matrizArchivo,
            String matrizNotas,
            Adjunto horarioArchivo,
            String horarioNotas,
            List<OffsetDateTime> horarios,
            String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.PROPONER_FECHAS, estadoEsperado, cuenta);
        String notasMatriz = notas(matrizArchivo, matrizNotas, "matriz", "la matriz de equivalencias");
        String notasHorario = notas(horarioArchivo, horarioNotas, "horario", "el posible horario de cursada");
        reuniones.abrirRonda(solicitud, 1, cuenta, horarios);
        guardar(solicitud, Tipo.MATRIZ_EQUIVALENCIAS, matrizArchivo, notasMatriz, (Usuario) cuenta, null);
        guardar(solicitud, Tipo.HORARIO_CURSADA, horarioArchivo, notasHorario, (Usuario) cuenta, null);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /**
     * En `ANAL-POST` (RF-ACAD-004): versión nueva de la matriz o del horario de cursada, que reemplaza entera a la
     * anterior. El estado no cambia y se le avisa al aspirante. Devuelve los dos documentos vigentes.
     */
    @Transactional
    public List<AnalisisAcademico> actualizar(
            SesionActual actual, Integer id, String tipo, Adjunto archivo, String notas, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        transiciones.exigirQuePuedaActuar(cuenta, solicitud);
        if (!Estado.ANAL_POST.equals(solicitud.getEstadoCodigo())) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        Tipo deTipo = Arrays.stream(Tipo.values())
                .filter(valor -> valor.name().equals(tipo))
                .findFirst()
                .orElseThrow(() -> ApiException.enCampo(
                        ErrorCode.VALIDACION, "tipo", "Elegí la matriz de equivalencias o el horario de cursada."));
        String nombre = deTipo == Tipo.MATRIZ_EQUIVALENCIAS ? "la matriz de equivalencias" : "el horario de cursada";
        String notasLimpias = notas(archivo, notas, "notas", nombre);
        AnalisisAcademico anterior = vigentes(solicitud).stream()
                .filter(documento -> documento.getTipo() == deTipo)
                .findFirst()
                .orElseThrow();
        guardar(solicitud, deTipo, archivo, notasLimpias, (Usuario) cuenta, anterior);
        solicitud.modificada(OffsetDateTime.now());
        auditoria.registrarEn(
                cuenta,
                "actualizar_analisis",
                "solicitud",
                solicitud.getId(),
                Map.of("tipo", deTipo.name(), "version", anterior.getVersion() + 1),
                ip);
        notificaciones.analisisActualizado(solicitud, nombre);
        return vigentes(solicitud);
    }

    /** Transición 14 (RF-ACAD-006): `ANAL-POST` → `INSC-ACEP`. */
    @Transactional
    public DetalleSolicitud aceptarInscripcion(SesionActual actual, Integer id, String estadoEsperado, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ACEPTAR_INSCRIPCION, estadoEsperado, cuenta);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Transición 15 (RF-ACAD-006): `ANAL-POST` → `INSC-RECH`, que es terminal. El motivo lo ve el aspirante. */
    @Transactional
    public DetalleSolicitud rechazarInscripcion(
            SesionActual actual, Integer id, String estadoEsperado, String comentario, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.RECHAZAR_INSCRIPCION, estadoEsperado, cuenta);
        if (comentario == null || comentario.isBlank()) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "comentario", "Indicá el motivo del rechazo.");
        }
        if (comentario.strip().length() > MAX_COMENTARIO) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "comentario", "Máximo " + MAX_COMENTARIO + " caracteres.");
        }
        transiciones.aplicar(solicitud, transicion, cuenta, comentario.strip(), ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Mismas reglas de archivo que los documentos del aspirante: PDF o JPG de hasta 10 MB. */
    private void guardar(
            Solicitud solicitud, Tipo tipo, Adjunto archivo, String notas, Usuario autor, AnalisisAcademico anterior) {
        String nombre = null;
        String ruta = null;
        if (archivo != null) {
            String extension = DocumentoService.extension(archivo.contenido());
            if (archivo.contenido().length > DocumentoService.MAX_ARCHIVO) {
                throw new ApiException(ErrorCode.LIMITE_TAMANO, "El archivo supera los 10 MB.");
            }
            ruta = archivos.guardar(DocumentoService.carpeta(solicitud), archivo.contenido(), extension);
            archivos.borrarSiSeDeshace(ruta);
            nombre = DocumentoService.nombreVisible(archivo.nombre(), extension);
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        analisis.save(
                anterior == null
                        ? new AnalisisAcademico(solicitud.getId(), tipo, nombre, ruta, notas, autor, ahora)
                        : anterior.nuevaVersion(nombre, ruta, notas, autor, ahora));
    }

    private Solicitud paraActualizar(Integer id) {
        return solicitudes.buscarParaActualizar(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    List<AnalisisAcademico> vigentes(Solicitud solicitud) {
        Map<Tipo, AnalisisAcademico> porTipo = new EnumMap<>(Tipo.class);
        analisis.findBySolicitudIdOrderByVersionDescIdAsc(solicitud.getId())
                .forEach(documento -> porTipo.putIfAbsent(documento.getTipo(), documento));
        return List.copyOf(porTipo.values());
    }

    private Solicitud visible(Cuenta cuenta, Integer solicitudId) {
        return solicitudes.findById(solicitudId)
                .filter(solicitud -> solicitud.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    /** Cada documento lleva archivo, notas o las dos cosas (RF-ACAD-002). */
    private static String notas(Adjunto archivo, String notas, String campo, String nombre) {
        String limpias = notas == null || notas.isBlank() ? null : notas.strip();
        if (archivo == null && limpias == null) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, campo, "Cargá " + nombre + ": un archivo, notas o las dos cosas.");
        }
        if (limpias != null && limpias.length() > MAX_NOTAS) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, campo, "Las notas admiten hasta " + MAX_NOTAS + " caracteres.");
        }
        return limpias;
    }
}
