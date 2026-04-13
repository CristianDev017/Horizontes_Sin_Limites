package com.horizontes.servlet;

import com.horizontes.dao.PagoDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.model.Pago;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/api/factura/*")
public class FacturaServlet extends HttpServlet {

    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    // GET /api/factura/{reservacionId}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) {
            res.sendError(403, "Acceso denegado"); return;
        }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            res.sendError(400, "ID requerido"); return;
        }

        int reservacionId = Integer.parseInt(path.substring(1));
        Reservacion r = reservacionDAO.buscarPorId(reservacionId);
        if (r == null) { res.sendError(404, "Reservacion no encontrada"); return; }

        List<Pago> pagos = pagoDAO.listarPorReservacion(reservacionId);

        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=factura-" + r.getNumero() + ".pdf");

        try {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, res.getOutputStream());
            doc.open();

            // Fuentes
            Font fuenteTitulo  = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(0, 100, 0));
            Font fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
            Font fuenteNormal  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font fuenteNegrita = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font fuenteBlanca  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

            // Encabezado
            Paragraph titulo = new Paragraph("Horizontes sin Límites", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph subtitulo = new Paragraph("Agencia de Viajes", fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitulo);

            doc.add(new Paragraph(" "));

            LineSeparator linea = new LineSeparator();
            linea.setLineColor(new BaseColor(0, 100, 0));
            doc.add(new Chunk(linea));
            doc.add(new Paragraph(" "));

            // Título factura
            Paragraph tituloFactura = new Paragraph("FACTURA DE RESERVACIÓN", fuenteSubtitulo);
            tituloFactura.setAlignment(Element.ALIGN_CENTER);
            doc.add(tituloFactura);
            doc.add(new Paragraph(" "));

            // Datos de la reservación
            PdfPTable tablaInfo = new PdfPTable(2);
            tablaInfo.setWidthPercentage(100);
            tablaInfo.setSpacingBefore(10);

            agregarFilaTabla(tablaInfo, "Número de Reservación:", r.getNumero(), fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaInfo, "Paquete:", r.getPaqueteNombre(), fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaInfo, "Agente:", r.getAgenteNombre(), fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaInfo, "Fecha de Viaje:", formatearFecha(r.getFechaViaje()), fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaInfo, "Estado:", r.getEstado(), fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaInfo, "Costo Total:", "Q." + r.getCostoTotal(), fuenteNegrita, fuenteNormal);
            doc.add(tablaInfo);

            doc.add(new Paragraph(" "));

            // Tabla de pagos
            Paragraph tituloPagos = new Paragraph("Pagos Registrados", fuenteSubtitulo);
            doc.add(tituloPagos);
            doc.add(new Paragraph(" "));

            PdfPTable tablaPagos = new PdfPTable(3);
            tablaPagos.setWidthPercentage(100);
            tablaPagos.setWidths(new float[]{2f, 2f, 2f});

            // Encabezado tabla pagos
            String[] encabezados = {"Fecha", "Método", "Monto"};
            for (String enc : encabezados) {
                PdfPCell cell = new PdfPCell(new Phrase(enc, fuenteBlanca));
                cell.setBackgroundColor(new BaseColor(0, 100, 0));
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaPagos.addCell(cell);
            }

            // Filas de pagos
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Pago p : pagos) {
                PdfPCell cFecha = new PdfPCell(new Phrase(sdf.format(p.getFecha()), fuenteNormal));
                cFecha.setPadding(6);
                cFecha.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell cMetodo = new PdfPCell(new Phrase(getMetodoNombre(p.getMetodo()), fuenteNormal));
                cMetodo.setPadding(6);
                cMetodo.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell cMonto = new PdfPCell(new Phrase("Q." + p.getMonto(), fuenteNormal));
                cMonto.setPadding(6);
                cMonto.setHorizontalAlignment(Element.ALIGN_CENTER);

                tablaPagos.addCell(cFecha);
                tablaPagos.addCell(cMetodo);
                tablaPagos.addCell(cMonto);
            }
            doc.add(tablaPagos);

            doc.add(new Paragraph(" "));
            java.math.BigDecimal totalPagado = pagoDAO.totalPagado(reservacionId);
            java.math.BigDecimal pendiente = r.getCostoTotal().subtract(totalPagado);

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(50);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            agregarFilaTabla(tablaTotales, "Total Pagado:", "Q." + totalPagado, fuenteNegrita, fuenteNormal);
            agregarFilaTabla(tablaTotales, "Saldo Pendiente:", "Q." + pendiente, fuenteNegrita, fuenteNormal);
            doc.add(tablaTotales);

            doc.add(new Paragraph(" "));
            doc.add(new Chunk(linea));
            doc.add(new Paragraph(" "));
            Paragraph pie = new Paragraph("Gracias por confiar en Horizontes sin Límites", fuenteNormal);
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);

            doc.close();

        } catch (DocumentException e) {
            System.out.println("Error generar PDF: " + e);
        }
    }

    private void agregarFilaTabla(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPadding(4);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fValor));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPadding(4);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private String formatearFecha(java.util.Date fecha) {
        if (fecha == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy").format(fecha);
    }

    private String getMetodoNombre(int metodo) {
        if (metodo == 1) return "Efectivo";
        if (metodo == 2) return "Tarjeta";
        return "Transferencia";
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean esAtencionOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 1 || u.getTipo() == 3);
    }
}