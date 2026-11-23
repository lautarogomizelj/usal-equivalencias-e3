package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.DocumentoSolicitud.EstadoRevision;
import com.usal.e3.domain.DocumentoSolicitud.Tipo;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.DocumentoRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Documentos del aspirante (RF-ASP-002 y RF-ASP-004, contrato §6.5). Cada archivo se sube al elegirlo y queda
 * como borrador, visible solo para su dueño, hasta ENVIAR_DOCUMENTACION.
 */
@Service
public class DocumentoService {

    static final long MAX_ARCHIVO = 10L * 1024 * 1024;
    private static final long MAX_ENVIO = 50L * 1024 * 1024;

    /** RF-ASP-002 y Anexo A #3 de interfaces: Analítico, Plan de estudios y al menos un Programa. */
    private static final List<Tipo> OBLIGATORIOS = List.of(Tipo.ANALITICO, Tipo.PLAN, Tipo.PROGRAMA);

    private static final Map<Tipo, String> NOMBRES =
            Map.of(Tipo.ANALITICO, "el analítico", Tipo.PLAN, "el plan de estudios", Tipo.PROGRAMA, "al menos un programa");

    private final SolicitudRepository solicitudes;
    private final DocumentoRepository documentos;
    private final CuentaService cuentas;
    private final ArchivoService archivos;

    public DocumentoService(
            SolicitudRepository solicitudes,
            DocumentoRepository documentos,
            CuentaService cuentas,
            ArchivoService archivos) {
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.cuentas = cuentas;
        this.archivos = archivos;
    }

    @Transactional(readOnly = true)
    public List<DocumentoSolicitud> listar(SesionActual actual, Integer solicitudId, boolean conVersiones) {
        Cuenta cuenta = cuentas.de(actual);
        return visibles(cuenta, visible(cuenta, solicitudId), conVersiones);
    }

    /**
     * En `SOL-INI` son documentos nuevos. En `DOC-RECH` y `LEG-RECH` cada archivo reemplaza a un documento
     * marcado para recargar (`reemplazaAId`), del que hereda tipo, descripción y etapa de revisión.
     */
    @Transactional
    public DocumentoSolicitud subir(
            SesionActual actual,
            Integer solicitudId,
            String tipo,
            String descripcion,
            Integer reemplazaAId,
            String nombreOriginal,
            byte[] contenido) {
        Solicitud solicitud = propia(cuentas.de(actual), solicitudId);
        DocumentoSolicitud anterior = null;
        if (Estado.DOC_RECH.equals(solicitud.getEstadoCodigo())
                || Estado.LEG_RECH.equals(solicitud.getEstadoCodigo())) {
            anterior = aRecargar(solicitud, reemplazaAId);
        } else if (!Estado.SOL_INI.equals(solicitud.getEstadoCodigo())) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        Tipo tipoDocumento = anterior == null ? tipo(tipo) : anterior.getTipo();
        String descripcionLimpia = anterior == null ? descripcion(descripcion) : anterior.getDescripcion();
        String extension = extension(contenido);
        if (contenido.length > MAX_ARCHIVO) {
            throw new ApiException(ErrorCode.LIMITE_TAMANO, "El archivo supera los 10 MB.");
        }
        // Elegir otro archivo para el mismo documento descarta el que se había subido antes.
        Integer reemplazado = anterior == null ? null : anterior.getId();
        List<DocumentoSolicitud> borradores = borradores(solicitud);
        List<DocumentoSolicitud> descartados = borradores.stream()
                .filter(borrador -> reemplazado != null && reemplazado.equals(borrador.getReemplazaAId()))
                .toList();
        long enBorrador = borradores.stream()
                .filter(borrador -> !descartados.contains(borrador))
                .mapToLong(DocumentoSolicitud::getTamanoBytes)
                .sum();
        if (enBorrador + contenido.length > MAX_ENVIO) {
            throw new ApiException(ErrorCode.LIMITE_TAMANO, "Entre todos los archivos no pueden superar los 50 MB.");
        }
        String ruta = archivos.guardar(carpeta(solicitud), contenido, extension);
        archivos.borrarSiSeDeshace(ruta);
        // Recién con el archivo nuevo aceptado y guardado.
        descartados.forEach(this::eliminar);
        OffsetDateTime ahora = OffsetDateTime.now();
        solicitud.modificada(ahora);
        DocumentoSolicitud nuevo = new DocumentoSolicitud(
                solicitud.getId(),
                tipoDocumento,
                descripcionLimpia,
                nombreVisible(nombreOriginal, extension),
                ruta,
                contenido.length,
                ahora);
        if (anterior != null) {
            nuevo.reemplazar(anterior);
        }
        return documentos.save(nuevo);
    }

    /** Solo los borradores: lo enviado ya no se quita. */
    @Transactional
    public void quitar(SesionActual actual, Integer solicitudId, Integer documentoId) {
        Solicitud solicitud = propia(cuentas.de(actual), solicitudId);
        DocumentoSolicitud documento = documentos.findByIdAndSolicitudId(documentoId, solicitudId)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        if (!documento.isBorrador()) {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }
        eliminar(documento);
        solicitud.modificada(OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga archivo(SesionActual actual, Integer solicitudId, Integer documentoId) {
        Cuenta cuenta = cuentas.de(actual);
        DocumentoSolicitud documento = visibles(cuenta, visible(cuenta, solicitudId), true).stream()
                .filter(d -> d.getId().equals(documentoId) && d.getRutaArchivo() != null)
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        String contentType = documento.getRutaArchivo().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        return new ArchivoDescarga(
                archivos.leer(documento.getRutaArchivo()), documento.getNombreArchivo(), contentType);
    }

    /**
     * Lo que ve cada uno: los borradores solo el aspirante, y las versiones anteriores solo si se piden. Un
     * borrador no archiva al documento que reemplaza hasta que se envía: en la recarga el aspirante ve los dos.
     */
    List<DocumentoSolicitud> visibles(Cuenta cuenta, Solicitud solicitud, boolean conVersiones) {
        List<DocumentoSolicitud> todos = documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId());
        Set<Integer> archivados = archivados(todos);
        return todos.stream()
                .filter(d -> !d.isBorrador() || cuenta.tipo() == TitularTipo.ASPIRANTE)
                .filter(d -> conVersiones || !archivados.contains(d.getId()))
                .toList();
    }

    /** Los documentos enviados que siguen en pie: sobre ellos se marca y se decide (RF-ACAD-009). */
    static List<DocumentoSolicitud> vigentes(List<DocumentoSolicitud> todos) {
        Set<Integer> archivados = archivados(todos);
        return todos.stream()
                .filter(d -> !d.isBorrador() && !archivados.contains(d.getId()))
                .toList();
    }

    private static Set<Integer> archivados(List<DocumentoSolicitud> todos) {
        return todos.stream()
                .filter(d -> !d.isBorrador())
                .map(DocumentoSolicitud::getReemplazaAId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Antes de pasar a `DOC-REV`: exige los obligatorios y deja de ser borrador lo cargado. */
    void enviarBorradores(Solicitud solicitud) {
        List<DocumentoSolicitud> borradores = borradores(solicitud);
        Set<Tipo> cargados = borradores.stream().map(DocumentoSolicitud::getTipo).collect(Collectors.toSet());
        List<String> faltan = OBLIGATORIOS.stream()
                .filter(tipo -> !cargados.contains(tipo))
                .map(NOMBRES::get)
                .toList();
        if (!faltan.isEmpty()) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "documentos", "Falta cargar " + String.join(", ", faltan) + ".");
        }
        borradores.forEach(DocumentoSolicitud::enviar);
    }

    /** Al pasar a `LEG-REV` desde `INSC-ACEP` o `LEG-PEND`: los documentos vigentes se revisan de nuevo. */
    void iniciarLegalizacion(Solicitud solicitud) {
        vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId()))
                .forEach(DocumentoSolicitud::iniciarLegalizacion);
    }

    /** Antes de volver a `DOC-REV` o a `LEG-REV`: cada documento a recargar tiene que traer su archivo nuevo. */
    void enviarRecarga(Solicitud solicitud) {
        List<DocumentoSolicitud> borradores = borradores(solicitud);
        Set<Integer> recargados =
                borradores.stream().map(DocumentoSolicitud::getReemplazaAId).collect(Collectors.toSet());
        List<String> faltan = vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).stream()
                .filter(d -> d.getEstadoRevision() == EstadoRevision.RECARGAR && !recargados.contains(d.getId()))
                .map(DocumentoSolicitud::titulo)
                .toList();
        if (!faltan.isEmpty()) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "documentos", "Falta recargar: " + String.join(", ", faltan) + ".");
        }
        borradores.forEach(DocumentoSolicitud::enviar);
    }

    /** Al cancelar en `SOL-INI`: lo que no se llegó a enviar se elimina, con su archivo. */
    void descartarBorradores(Solicitud solicitud) {
        borradores(solicitud).forEach(this::eliminar);
    }

    private void eliminar(DocumentoSolicitud borrador) {
        documentos.delete(borrador);
        archivos.borrarAlConfirmar(borrador.getRutaArchivo());
    }

    /** Solo se reemplaza lo que el Académico marcó con ❌: lo correcto queda bloqueado (RF-ASP-004). */
    private DocumentoSolicitud aRecargar(Solicitud solicitud, Integer reemplazaAId) {
        if (reemplazaAId == null) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "reemplazaAId", "Indicá qué documento reemplaza el archivo.");
        }
        DocumentoSolicitud anterior = vigentes(documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).stream()
                .filter(d -> d.getId().equals(reemplazaAId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        if (anterior.getEstadoRevision() != EstadoRevision.RECARGAR) {
            throw new ApiException(ErrorCode.DOCUMENTO_BLOQUEADO);
        }
        return anterior;
    }

    private List<DocumentoSolicitud> borradores(Solicitud solicitud) {
        return documentos.findBySolicitudIdOrderByIdAsc(solicitud.getId()).stream()
                .filter(DocumentoSolicitud::isBorrador)
                .toList();
    }

    private Solicitud visible(Cuenta cuenta, Integer solicitudId) {
        return solicitudes.findById(solicitudId)
                .filter(solicitud -> solicitud.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    private Solicitud propia(Cuenta cuenta, Integer solicitudId) {
        return solicitudes.buscarParaActualizar(solicitudId)
                .filter(solicitud -> solicitud.esPropiaDe(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    static Tipo tipo(String tipo) {
        try {
            return Tipo.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "tipo", "El tipo debe ser ANALITICO, PLAN, PROGRAMA u OTRO.");
        }
    }

    static String descripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        if (descripcion.strip().length() > 255) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "descripcion", "Máximo 255 caracteres.");
        }
        return descripcion.strip();
    }

    /** Por año y DNI (RF-ACAD-009), con una carpeta por expediente. */
    static String carpeta(Solicitud solicitud) {
        return solicitud.getFechaCreacion().getYear() + "/" + solicitud.getAspirante().getDni() + "/"
                + solicitud.getCodigoExp();
    }

    /** Por el contenido y no por el nombre: un ejecutable renombrado a `.pdf` no pasa. */
    static String extension(byte[] contenido) {
        if (contenido.length >= 4
                && contenido[0] == '%'
                && contenido[1] == 'P'
                && contenido[2] == 'D'
                && contenido[3] == 'F') {
            return "pdf";
        }
        if (contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        throw new ApiException(ErrorCode.ARCHIVO_INVALIDO);
    }

    /** Solo para mostrar: sin carpetas y con un máximo de 255 caracteres. */
    static String nombreVisible(String original, String extension) {
        String nombre = original == null ? "" : original.replaceAll(".*[/\\\\]", "").strip();
        if (nombre.isEmpty()) {
            return "documento." + extension;
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }
}
