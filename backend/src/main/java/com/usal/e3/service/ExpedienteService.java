package com.usal.e3.service;

import com.usal.e3.domain.AnalisisAcademico;
import com.usal.e3.domain.ComprobantePago;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.CuponPago;
import com.usal.e3.domain.DocumentoSolicitud;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.ComprobantePagoRepository;
import com.usal.e3.repository.CuponPagoRepository;
import com.usal.e3.repository.DocumentoRepository;
import com.usal.e3.repository.HistorialEstadoRepository;
import com.usal.e3.repository.InformeMateriasRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exportación de expedientes en ZIP (RF-PROC-005): un resumen en PDF, los documentos vigentes, el análisis, el
 * cupón y los comprobantes. Para el personal, el Admin y el Revisor; cada descarga queda en auditoría.
 */
@Service
public class ExpedienteService {

    private final SolicitudRepository solicitudes;
    private final DocumentoRepository documentos;
    private final HistorialEstadoRepository historial;
    private final CuponPagoRepository cupones;
    private final ComprobantePagoRepository comprobantes;
    private final InformeMateriasRepository informes;
    private final AnalisisService analisis;
    private final ProcesoService proceso;
    private final CatalogoService catalogo;
    private final ArchivoService archivos;
    private final CuentaService cuentas;
    private final AuditoriaService auditoria;

    public ExpedienteService(
            SolicitudRepository solicitudes,
            DocumentoRepository documentos,
            HistorialEstadoRepository historial,
            CuponPagoRepository cupones,
            ComprobantePagoRepository comprobantes,
            InformeMateriasRepository informes,
            AnalisisService analisis,
            ProcesoService proceso,
            CatalogoService catalogo,
            ArchivoService archivos,
            CuentaService cuentas,
            AuditoriaService auditoria) {
        this.solicitudes = solicitudes;
        this.documentos = documentos;
        this.historial = historial;
        this.cupones = cupones;
        this.comprobantes = comprobantes;
        this.informes = informes;
        this.analisis = analisis;
        this.proceso = proceso;
        this.catalogo = catalogo;
        this.archivos = archivos;
        this.cuentas = cuentas;
        this.auditoria = auditoria;
    }

    @Transactional
    public ArchivoDescarga individual(SesionActual actual, Integer id, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = solicitudes.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        Resource zip = comprimir(List.of(solicitud), false);
        auditoria.registrarEn(cuenta, "descarga_expediente", "solicitud", id, null, ip);
        return new ArchivoDescarga(zip, "Expediente_" + solicitud.getCodigoExp() + ".zip", "application/zip");
    }

    /** Las solicitudes de la bandeja con esos filtros, cada una en su carpeta: `Expedientes_2026_[fecha].zip`. */
    @Transactional
    public ArchivoDescarga multiple(SesionActual actual, FiltroBandeja filtro, String ip) {
        List<Solicitud> elegidas = proceso.bandeja(actual, filtro, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(ResumenSolicitud::solicitud)
                .toList();
        if (elegidas.isEmpty()) {
            throw new ApiException(ErrorCode.NO_ENCONTRADO, "No hay solicitudes para exportar con esos filtros.");
        }
        Resource zip = comprimir(elegidas, true);
        auditoria.registrarEn(
                cuentas.de(actual),
                "descarga_expedientes",
                "solicitud",
                null,
                Map.of("expedientes", elegidas.stream().map(Solicitud::getCodigoExp).toList()),
                ip);
        LocalDate hoy = LocalDate.now(ProcesoService.ZONA);
        return new ArchivoDescarga(zip, "Expedientes_" + hoy.getYear() + "_" + hoy + ".zip", "application/zip");
    }

    /** A un archivo temporal, que se borra solo cuando termina la descarga: un expediente puede pesar 50 MB. */
    private Resource comprimir(List<Solicitud> elegidas, boolean enCarpetas) {
        Map<String, Estado> estados = catalogo.estadosPorCodigo();
        try {
            Path temporal = Files.createTempFile("expedientes-", ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temporal))) {
                for (Solicitud solicitud : elegidas) {
                    agregar(zip, enCarpetas ? solicitud.getCodigoExp() + "/" : "", solicitud, estados);
                }
            }
            return new InputStreamResource(Files.newInputStream(temporal, StandardOpenOption.DELETE_ON_CLOSE));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void agregar(ZipOutputStream zip, String carpeta, Solicitud solicitud, Map<String, Estado> estados)
            throws IOException {
        Integer id = solicitud.getId();
        CuponPago cupon = cupones.findBySolicitudId(id).orElse(null);
        zip.putNextEntry(new ZipEntry(carpeta + "resumen.pdf"));
        zip.write(ExpedientePdf.generar(
                solicitud,
                estados,
                historial.findBySolicitudIdOrderByFechaAsignacionDescIdDesc(id),
                informes.findBySolicitudId(id).orElse(null),
                cupon));
        zip.closeEntry();

        int numero = 0;
        for (DocumentoSolicitud documento : DocumentoService.vigentes(documentos.findBySolicitudIdOrderByIdAsc(id))) {
            if (documento.tieneArchivo()) {
                String nombre = String.format("%02d-%s", ++numero, documento.getNombreArchivo());
                copiar(zip, carpeta + "documentos/" + nombre, documento.getRutaArchivo());
            }
        }
        for (AnalisisAcademico documento : analisis.vigentes(solicitud)) {
            if (documento.getRutaArchivo() != null) {
                String tipo = documento.getTipo() == AnalisisAcademico.Tipo.MATRIZ_EQUIVALENCIAS ? "matriz" : "horario";
                copiar(zip, carpeta + "analisis/" + tipo + "-" + documento.getNombreArchivo(), documento.getRutaArchivo());
            }
        }
        if (cupon != null) {
            copiar(zip, carpeta + "pago/cupon-" + solicitud.getCodigoExp() + ".pdf", cupon.getRutaArchivo());
        }
        for (ComprobantePago comprobante : comprobantes.findBySolicitudIdOrderByVersionAsc(id)) {
            String nombre = "comprobante-v" + comprobante.getVersion() + "-" + comprobante.getNombreArchivo();
            copiar(zip, carpeta + "pago/" + nombre, comprobante.getRutaArchivo());
        }
    }

    /** El nombre visible lo eligió quien subió el archivo: sin barras, para que no arme carpetas dentro del ZIP. */
    private void copiar(ZipOutputStream zip, String nombre, String ruta) throws IOException {
        Resource archivo = archivos.leer(ruta);
        if (!archivo.exists()) {
            return;
        }
        int corte = nombre.lastIndexOf('/') + 1;
        zip.putNextEntry(new ZipEntry(nombre.substring(0, corte) + nombre.substring(corte).replaceAll("[\\\\/]", "_")));
        try (InputStream entrada = archivo.getInputStream()) {
            entrada.transferTo(zip);
        }
        zip.closeEntry();
    }
}
