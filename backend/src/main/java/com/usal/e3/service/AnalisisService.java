package com.usal.e3.service;

import com.usal.e3.domain.AnalisisAcademico;
import com.usal.e3.domain.AnalisisAcademico.Tipo;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AnalisisRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Análisis preliminar (RF-ACAD-002): el Académico carga la matriz de equivalencias y el posible horario de
 * cursada y, en la misma acción, propone las fechas de la reunión. Actualizarlos desde `ANAL-POST` llega con su sprint.
 */
@Service
public class AnalisisService {

    private static final int MAX_NOTAS = 2000;

    /** Archivo que llegó en el pedido, con el nombre que traía. */
    public record Adjunto(String nombre, byte[] contenido) {}

    private final SolicitudRepository solicitudes;
    private final AnalisisRepository analisis;
    private final CuentaService cuentas;
    private final ArchivoService archivos;
    private final TransicionService transiciones;
    private final ReunionService reuniones;
    private final SolicitudService detalles;

    public AnalisisService(
            SolicitudRepository solicitudes,
            AnalisisRepository analisis,
            CuentaService cuentas,
            ArchivoService archivos,
            TransicionService transiciones,
            ReunionService reuniones,
            SolicitudService detalles) {
        this.solicitudes = solicitudes;
        this.analisis = analisis;
        this.cuentas = cuentas;
        this.archivos = archivos;
        this.transiciones = transiciones;
        this.reuniones = reuniones;
        this.detalles = detalles;
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
        Solicitud solicitud = solicitudes.buscarParaActualizar(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.PROPONER_FECHAS, estadoEsperado, cuenta);
        String notasMatriz = notas(matrizArchivo, matrizNotas, "matriz", "la matriz de equivalencias");
        String notasHorario = notas(horarioArchivo, horarioNotas, "horario", "el posible horario de cursada");
        reuniones.abrirRonda(solicitud, 1, cuenta, horarios);
        guardar(solicitud, Tipo.MATRIZ_EQUIVALENCIAS, matrizArchivo, notasMatriz, (Usuario) cuenta);
        guardar(solicitud, Tipo.HORARIO_CURSADA, horarioArchivo, notasHorario, (Usuario) cuenta);
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    /** Mismas reglas de archivo que los documentos del aspirante: PDF o JPG de hasta 10 MB. */
    private void guardar(Solicitud solicitud, Tipo tipo, Adjunto archivo, String notas, Usuario autor) {
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
        analisis.save(new AnalisisAcademico(solicitud.getId(), tipo, nombre, ruta, notas, autor, OffsetDateTime.now()));
    }

    private List<AnalisisAcademico> vigentes(Solicitud solicitud) {
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
