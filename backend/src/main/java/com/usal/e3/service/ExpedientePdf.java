package com.usal.e3.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.usal.e3.domain.CuponPago;
import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.HistorialEstado;
import com.usal.e3.domain.InformeMaterias;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.Usuario;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** El resumen que encabeza cada expediente exportado (RF-PROC-005): datos, responsables e historial de estados. */
final class ExpedientePdf {

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ProcesoService.ZONA);
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 11);

    private ExpedientePdf() {}

    /** `cambios` viene del más nuevo al más viejo; `informe` y `cupon` son nulos si la solicitud no llegó ahí. */
    static byte[] generar(
            Solicitud solicitud,
            Map<String, Estado> estados,
            List<HistorialEstado> cambios,
            InformeMaterias informe,
            CuponPago cupon) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 56, 56, 56, 56);
        PdfWriter.getInstance(documento, salida);
        documento.open();
        documento.add(new Paragraph("Universidad del Salvador", TEXTO));
        documento.add(new Paragraph("Expediente " + solicitud.getCodigoExp(), TITULO));
        documento.add(new Paragraph("Trámite de equivalencias", TEXTO));

        seccion(documento, "Estado");
        linea(documento, "Estado actual", estado(estados, solicitud.getEstadoCodigo()));
        linea(documento, "Creada", FECHA_HORA.format(solicitud.getFechaCreacion()));
        linea(documento, "Última actualización", FECHA_HORA.format(solicitud.getFechaUltimaMod()));
        linea(documento, "Responsable académico", responsable(solicitud.getResponsableAcademico()));
        linea(documento, "Responsable administrativo", responsable(solicitud.getResponsableAdministrativo()));

        seccion(documento, "Aspirante");
        linea(documento, "Nombre", solicitud.getAspirante().nombreCompleto());
        linea(documento, "DNI", solicitud.getAspirante().getDni());
        linea(documento, "Email", solicitud.getAspirante().getEmail());
        linea(documento, "Teléfono", solicitud.getAspirante().getTelefono());

        DatosOrigen origen = solicitud.getDatosOrigen();
        seccion(documento, "Solicitud");
        linea(documento, "Carrera destino", solicitud.getCarreraDestino().getNombre());
        linea(documento, "Universidad de origen", origen.getUniversidad());
        linea(documento, "Facultad de origen", origen.getFacultad());
        linea(documento, "Carrera de origen", origen.getCarrera());
        linea(documento, "Condición académica", origen.getCondicionAcademica());
        linea(documento, "Observaciones", origen.getObservaciones());

        if (informe != null) {
            seccion(documento, "Informe de materias");
            linea(documento, "Materias reconocidas", String.valueOf(informe.getMateriasReconocidas()));
            linea(documento, "Observaciones", informe.getObservaciones());
            linea(documento, "Monto del cupón", cupon == null ? null : CuponPdf.monto(cupon.getMonto()));
        }

        seccion(documento, "Historial de estados");
        for (int i = cambios.size() - 1; i >= 0; i--) {
            HistorialEstado cambio = cambios.get(i);
            String quien = cambio.getUsuarioUltimoCambio() == null
                    ? solicitud.getAspirante().nombreCompleto()
                    : cambio.getUsuarioUltimoCambio().nombreCompleto();
            String desde = cambio.getEstadoAnteriorCodigo() == null ? "" : cambio.getEstadoAnteriorCodigo() + " → ";
            String comentario = cambio.getComentarios() == null ? "" : ". " + cambio.getComentarios();
            documento.add(new Paragraph(
                    FECHA_HORA.format(cambio.getFechaAsignacion()) + " · " + desde
                            + estado(estados, cambio.getEstadoCodigo()) + " · " + quien + comentario,
                    TEXTO));
        }
        documento.close();
        return salida.toByteArray();
    }

    private static void seccion(Document documento, String titulo) {
        documento.add(new Paragraph(" ", TEXTO));
        documento.add(new Paragraph(titulo, SUBTITULO));
    }

    private static void linea(Document documento, String etiqueta, String valor) {
        documento.add(new Paragraph(etiqueta + ": " + (valor == null || valor.isBlank() ? "—" : valor), TEXTO));
    }

    private static String estado(Map<String, Estado> estados, String codigo) {
        return codigo + " (" + estados.get(codigo).getNombre() + ")";
    }

    private static String responsable(Usuario usuario) {
        return usuario == null ? null : usuario.nombreCompleto() + " <" + usuario.getEmail() + ">";
    }
}
