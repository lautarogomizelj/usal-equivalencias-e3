package com.usal.e3.service;

import com.usal.e3.domain.ComprobantePago;
import com.usal.e3.domain.ComprobantePago.Verificacion;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.CuponPago;
import com.usal.e3.domain.InformeMaterias;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.ComprobantePagoRepository;
import com.usal.e3.repository.CuponPagoRepository;
import com.usal.e3.repository.InformeMateriasRepository;
import com.usal.e3.repository.SolicitudRepository;
import com.usal.e3.security.SesionActual;
import com.usal.e3.service.AnalisisService.Adjunto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Etapa de pago (RF-SEC-001, RF-ASP-007 y RF-SEC-002): Secretaría genera el cupón con el monto, el aspirante
 * carga el comprobante y Secretaría lo acepta o lo rechaza. También el informe de materias (RF-ASP-006).
 */
@Service
public class PagoService {

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");
    private static final int MAX_COMENTARIO = 500;

    /** `cupon` es `null` hasta que Secretaría lo envía. */
    public record Info(CuponPago cupon, List<ComprobantePago> comprobantes) {}

    /** `montoAPagar` es `null` hasta que existe el cupón. */
    public record Informe(InformeMaterias informe, BigDecimal montoAPagar) {}

    private final SolicitudRepository solicitudes;
    private final CuponPagoRepository cupones;
    private final ComprobantePagoRepository comprobantes;
    private final InformeMateriasRepository informes;
    private final CuentaService cuentas;
    private final ArchivoService archivos;
    private final TransicionService transiciones;
    private final SolicitudService detalles;

    public PagoService(
            SolicitudRepository solicitudes,
            CuponPagoRepository cupones,
            ComprobantePagoRepository comprobantes,
            InformeMateriasRepository informes,
            CuentaService cuentas,
            ArchivoService archivos,
            TransicionService transiciones,
            SolicitudService detalles) {
        this.solicitudes = solicitudes;
        this.cupones = cupones;
        this.comprobantes = comprobantes;
        this.informes = informes;
        this.cuentas = cuentas;
        this.archivos = archivos;
        this.transiciones = transiciones;
        this.detalles = detalles;
    }

    @Transactional(readOnly = true)
    public Info info(SesionActual actual, Integer solicitudId) {
        Solicitud solicitud = visible(cuentas.de(actual), solicitudId);
        return new Info(
                cupones.findBySolicitudId(solicitud.getId()).orElse(null),
                comprobantes.findBySolicitudIdOrderByVersionAsc(solicitud.getId()));
    }

    /** Existe desde `CUPON-SOL`: antes responde 404. */
    @Transactional(readOnly = true)
    public Informe informe(SesionActual actual, Integer solicitudId) {
        Solicitud solicitud = visible(cuentas.de(actual), solicitudId);
        InformeMaterias informe = informes.findBySolicitudId(solicitud.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        return new Informe(
                informe,
                cupones.findBySolicitudId(solicitud.getId()).map(CuponPago::getMonto).orElse(null));
    }

    /** Transición 22 (RF-SEC-001): `CUPON-SOL` → `CUPON-PAG`. El sistema genera el PDF con el monto indicado. */
    @Transactional
    public DetalleSolicitud enviarCupon(
            SesionActual actual, Integer id, String estadoEsperado, BigDecimal monto, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion = transiciones.validar(solicitud, TransicionService.ENVIAR_CUPON, estadoEsperado, cuenta);
        if (monto == null || monto.signum() <= 0) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "monto", "Indicá el monto del cupón.");
        }
        if (monto.scale() > 2 || monto.compareTo(MONTO_MAXIMO) > 0) {
            throw ApiException.enCampo(
                    ErrorCode.VALIDACION, "monto", "El monto admite hasta 2 decimales y un máximo de 99.999.999,99.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        String ruta = archivos.guardar(carpeta(solicitud), CuponPdf.generar(solicitud, monto, ahora), "pdf");
        archivos.borrarSiSeDeshace(ruta);
        cupones.save(new CuponPago(solicitud.getId(), monto, ruta, (Usuario) cuenta, ahora));
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga cuponArchivo(SesionActual actual, Integer solicitudId) {
        Solicitud solicitud = visible(cuentas.de(actual), solicitudId);
        CuponPago cupon = cupones.findBySolicitudId(solicitud.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        return new ArchivoDescarga(
                archivos.leer(cupon.getRutaArchivo()), "cupon-" + solicitud.getCodigoExp() + ".pdf", "application/pdf");
    }

    /** Transiciones 23 y 25 (RF-ASP-007): `CUPON-PAG` o `PAG-REENV` → `PAG-CTRL`. PDF o JPG de hasta 10 MB. */
    @Transactional
    public DetalleSolicitud enviarComprobante(
            SesionActual actual, Integer id, String estadoEsperado, Adjunto archivo, String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion =
                transiciones.validar(solicitud, TransicionService.ENVIAR_COMPROBANTE, estadoEsperado, cuenta);
        if (archivo == null) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "archivo", "Cargá el comprobante de pago.");
        }
        String extension = DocumentoService.extension(archivo.contenido());
        if (archivo.contenido().length > DocumentoService.MAX_ARCHIVO) {
            throw new ApiException(ErrorCode.LIMITE_TAMANO, "El archivo supera los 10 MB.");
        }
        CuponPago cupon = cupones.findBySolicitudId(solicitud.getId()).orElseThrow();
        String ruta = archivos.guardar(carpeta(solicitud), archivo.contenido(), extension);
        archivos.borrarSiSeDeshace(ruta);
        comprobantes.save(new ComprobantePago(
                cupon,
                comprobantes.findBySolicitudIdOrderByVersionAsc(solicitud.getId()).size() + 1,
                DocumentoService.nombreVisible(archivo.nombre(), extension),
                ruta,
                archivo.contenido().length,
                OffsetDateTime.now()));
        transiciones.aplicar(solicitud, transicion, cuenta, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga comprobanteArchivo(SesionActual actual, Integer solicitudId, Integer comprobanteId) {
        Solicitud solicitud = visible(cuentas.de(actual), solicitudId);
        ComprobantePago comprobante = comprobantes.findById(comprobanteId)
                .filter(c -> c.getSolicitudId().equals(solicitud.getId()))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
        String contentType = comprobante.getRutaArchivo().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        return new ArchivoDescarga(
                archivos.leer(comprobante.getRutaArchivo()), comprobante.getNombreArchivo(), contentType);
    }

    /** Transición 26 (RF-SEC-002): `PAG-CTRL` → `E3-INI`. El comentario es opcional. */
    @Transactional
    public DetalleSolicitud aceptar(SesionActual actual, Integer id, String estadoEsperado, String comentario, String ip) {
        return verificar(actual, id, estadoEsperado, TransicionService.ACEPTAR_PAGO, Verificacion.ACEPTADO, comentario, ip);
    }

    /** Transición 24 (RF-SEC-002): `PAG-CTRL` → `PAG-REENV`. El comentario es obligatorio y lo ve el aspirante. */
    @Transactional
    public DetalleSolicitud rechazar(SesionActual actual, Integer id, String estadoEsperado, String comentario, String ip) {
        return verificar(actual, id, estadoEsperado, TransicionService.RECHAZAR_PAGO, Verificacion.RECHAZADO, comentario, ip);
    }

    private DetalleSolicitud verificar(
            SesionActual actual,
            Integer id,
            String estadoEsperado,
            String accion,
            Verificacion resultado,
            String comentario,
            String ip) {
        Cuenta cuenta = cuentas.de(actual);
        Solicitud solicitud = paraActualizar(id);
        Transicion transicion = transiciones.validar(solicitud, accion, estadoEsperado, cuenta);
        String limpio = comentario == null || comentario.isBlank() ? null : comentario.strip();
        if (limpio == null && resultado == Verificacion.RECHAZADO) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "comentario", "Indicá por qué se rechaza el pago.");
        }
        if (limpio != null && limpio.length() > MAX_COMENTARIO) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "comentario", "Máximo " + MAX_COMENTARIO + " caracteres.");
        }
        List<ComprobantePago> cargados = comprobantes.findBySolicitudIdOrderByVersionAsc(solicitud.getId());
        cargados.get(cargados.size() - 1).verificar(resultado, limpio, (Usuario) cuenta, OffsetDateTime.now());
        transiciones.aplicar(solicitud, transicion, cuenta, limpio, ip);
        return detalles.detalle(cuenta, solicitud);
    }

    private static String carpeta(Solicitud solicitud) {
        return DocumentoService.carpeta(solicitud) + "/pago";
    }

    private Solicitud visible(Cuenta cuenta, Integer solicitudId) {
        return solicitudes.findById(solicitudId)
                .filter(solicitud -> solicitud.visiblePara(cuenta))
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    private Solicitud paraActualizar(Integer id) {
        return solicitudes.buscarParaActualizar(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }
}
