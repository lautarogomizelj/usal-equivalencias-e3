package com.usal.e3.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.usal.e3.domain.Solicitud;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** El cupón de pago en PDF (RF-SEC-001): los datos de la solicitud y el monto que indicó Secretaría. */
final class CuponPdf {

    private static final Locale ARGENTINA = new Locale("es", "AR");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ProcesoService.ZONA);
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font DESTACADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 12);

    private CuponPdf() {}

    static byte[] generar(Solicitud solicitud, BigDecimal monto, OffsetDateTime emision) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 56, 56, 56, 56);
        PdfWriter.getInstance(documento, salida);
        documento.open();
        documento.add(new Paragraph("Universidad del Salvador", TEXTO));
        documento.add(new Paragraph("Cupón de pago · Trámite de equivalencias", TITULO));
        documento.add(new Paragraph(" ", TEXTO));
        documento.add(new Paragraph("Expediente: " + solicitud.getCodigoExp(), TEXTO));
        documento.add(new Paragraph("Aspirante: " + solicitud.getAspirante().nombreCompleto(), TEXTO));
        documento.add(new Paragraph("DNI: " + solicitud.getAspirante().getDni(), TEXTO));
        documento.add(new Paragraph("Carrera: " + solicitud.getCarreraDestino().getNombre(), TEXTO));
        documento.add(new Paragraph("Fecha de emisión: " + FECHA.format(emision), TEXTO));
        documento.add(new Paragraph(" ", TEXTO));
        documento.add(new Paragraph("Monto a pagar: " + monto(monto), DESTACADO));
        documento.add(new Paragraph(" ", TEXTO));
        documento.add(new Paragraph(
                "Una vez hecho el pago, cargá el comprobante en tu solicitud para que Secretaría lo verifique.", TEXTO));
        documento.close();
        return salida.toByteArray();
    }

    static String monto(BigDecimal monto) {
        return NumberFormat.getCurrencyInstance(ARGENTINA).format(monto);
    }
}
