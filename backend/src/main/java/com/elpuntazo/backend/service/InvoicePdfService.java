package com.elpuntazo.backend.service;

import com.elpuntazo.backend.entity.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Genera el PDF SIEMPRE a partir de los datos guardados en la base de datos
 * (nunca se almacena un archivo binario aparte). Asi la factura se puede
 * volver a descargar en cualquier momento, incluso si el vendedor perdio
 * el archivo original, y si mejoramos el diseño mas adelante las facturas
 * antiguas se veran con el diseño nuevo automaticamente.
 */
@Service
public class InvoicePdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(30, 30, 30));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(90, 90, 90));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(60, 60, 60));
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(20, 20, 20));
    private static final Color ACCENT = new Color(24, 90, 60);
    private static final DecimalFormat COP_FORMAT = new DecimalFormat("$#,###");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    public byte[] generate(Invoice invoice) {
        Document document = new Document(PageSize.LETTER, 50, 50, 40, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, invoice);
            addInvoiceMeta(document, invoice);
            addTotalsTable(document, invoice);
            addFooter(document, invoice);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("No fue posible generar el PDF. Intenta nuevamente.", e);
        }
    }

    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        Paragraph name = new Paragraph("EL PUNTAZO", TITLE_FONT);
        name.setAlignment(Element.ALIGN_LEFT);
        document.add(name);

        Paragraph subtitle = new Paragraph(
                "Liquidacion de " + invoice.getClientName(), SUBTITLE_FONT);
        subtitle.setSpacingAfter(15);
        document.add(subtitle);

        LineSeparator line = new LineSeparator();
        line.setLineColor(ACCENT);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addInvoiceMeta(Document document, Invoice invoice) throws DocumentException {
        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100);
        meta.setSpacingAfter(20);

        addMetaCell(meta, "Referencia", "Factura No. " + invoice.getInvoiceNumber());
        addMetaCell(meta, "Fecha", invoice.getCreatedAt().format(DATE_FORMAT));

        document.add(meta);
    }

    private void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph(label, LABEL_FONT));
        cell.addElement(new Paragraph(value, VALUE_FONT));
        table.addCell(cell);
    }

    private void addTotalsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.4f});
        table.setSpacingAfter(25);

        addRow(table, "Subtotal (Valor Base)", format(invoice.getBaseValue()), false);
        addRow(table, "Descuento Aplicado (" + trimPercentage(invoice.getDiscountPercentage()) + "%)",
                "-" + format(invoice.getDiscountValue()), false);
        addRow(table, "Flete de Envio", format(invoice.getShippingValue()), false);

        if (invoice.getShippingAssumedByCompany() != null
                && invoice.getShippingAssumedByCompany().compareTo(BigDecimal.ZERO) > 0) {
            addRow(table, "Flete Asumido por El Puntazo",
                    "-" + format(invoice.getShippingAssumedByCompany()), false);
        }

        if (invoice.getWarrantyDeduction() != null
                && invoice.getWarrantyDeduction().compareTo(BigDecimal.ZERO) > 0) {
            addRow(table, "Garantia / Devolucion",
                    "-" + format(invoice.getWarrantyDeduction()), false);
        }

        addRow(table, "TOTAL A PAGAR", format(invoice.getTotalValue()), true);

        document.add(table);
    }

    private void addRow(PdfPTable table, String label, String value, boolean isTotal) {
        Font labelFont = isTotal ? TOTAL_FONT : VALUE_FONT;
        Font valueFont = isTotal ? TOTAL_FONT : VALUE_FONT;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(isTotal ? Rectangle.TOP : Rectangle.NO_BORDER);
        labelCell.setBorderColor(ACCENT);
        labelCell.setPaddingTop(8);
        labelCell.setPaddingBottom(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(isTotal ? Rectangle.TOP : Rectangle.NO_BORDER);
        valueCell.setBorderColor(ACCENT);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(8);
        valueCell.setPaddingBottom(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, Invoice invoice) throws DocumentException {
        PdfPTable people = new PdfPTable(2);
        people.setWidthPercentage(100);
        people.setSpacingAfter(30);

        addMetaCell(people, "Cliente", invoice.getClientName());
        addMetaCell(people, "Vendedor", invoice.getSeller().getName());

        document.add(people);

        Paragraph thanks = new Paragraph(
                "¡Muchas gracias por confiar en nosotros!",
                new Font(Font.HELVETICA, 11, Font.ITALIC, ACCENT));
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        Paragraph warranty = new Paragraph(
                "Nota: si este pedido presenta algun reclamo de garantia, El Puntazo la asume " +
                "directamente y se debera generar una nueva factura de reemplazo.",
                new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 100, 100)));
        warranty.setSpacingBefore(20);
        warranty.setAlignment(Element.ALIGN_CENTER);
        document.add(warranty);

        Paragraph disclaimer = new Paragraph(
                "Este documento es una liquidacion comercial interna de El Puntazo y no constituye " +
                "una factura electronica ante la DIAN.",
                new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(140, 140, 140)));
        disclaimer.setSpacingBefore(10);
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        document.add(disclaimer);
    }

    private String format(BigDecimal value) {
        return COP_FORMAT.format(value).replace(",", ".");
    }

    private String trimPercentage(BigDecimal percentage) {
        return percentage.stripTrailingZeros().toPlainString();
    }
}
