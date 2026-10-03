package com.myeshopping.invoiceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myeshopping.invoiceservice.client.OrderClient;
import com.myeshopping.invoiceservice.dto.InvoiceRequest;
import com.myeshopping.invoiceservice.entity.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Builds the PDF invoice for an order. The invoice record (number, date) is created on first request and reused after. */
@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    public record PdfDocument(String filename, byte[] bytes) { }

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final Color GREEN = new Color(0x29, 0x5D, 0x4B);
    private static final Color LINE = new Color(0xD7, 0xD9, 0xCF);

    private final OrderClient orderClient;
    private final InvoiceService invoiceService;

    public PdfDocument invoicePdf(Long orderId) {
        JsonNode order = orderClient.getOrder(orderId);

        InvoiceRequest request = new InvoiceRequest();
        request.setOrderId(orderId);
        request.setPaymentId(0L); // no payment record is linked to orders yet
        request.setCustomerId(order.path("customerId").asLong());
        request.setAmount(order.path("totalAmount").asDouble());
        request.setCurrency("USD");
        Invoice invoice = invoiceService.generate(request);

        return new PdfDocument("invoice-" + invoice.getInvoiceNumber() + ".pdf", render(invoice, order));
    }

    private byte[] render(Invoice invoice, JsonNode order) {
        Font brand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, GREEN);
        Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font muted = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Font tableHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font total = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("MY E-SHOP", brand));
            document.add(new Paragraph("Invoice", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY)));
            document.add(new Paragraph(" "));

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.addCell(infoCell("Invoice number", invoice.getInvoiceNumber(), heading, normal));
            info.addCell(infoCell("Invoice date", invoice.getIssuedAt().format(DATE_TIME), heading, normal));
            info.addCell(infoCell("Order number", "#" + order.path("id").asLong(), heading, normal));
            info.addCell(infoCell("Order date", formatDate(order.path("createdAt").asText("")), heading, normal));
            info.addCell(infoCell("Customer ID", String.valueOf(order.path("customerId").asLong()), heading, normal));
            info.addCell(infoCell("Order status", order.path("status").asText("").replace('_', ' '), heading, normal));
            document.add(info);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Ship to", heading));
            document.add(new Paragraph(order.path("shippingAddress").asText("-"), normal));
            document.add(new Paragraph(" "));

            PdfPTable items = new PdfPTable(new float[] {1.4f, 3.6f, 0.9f, 1.4f, 1.5f});
            items.setWidthPercentage(100);
            for (String header : new String[] {"SKU", "Item", "Qty", "Unit price", "Amount"}) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHead));
                cell.setBackgroundColor(GREEN);
                cell.setPadding(6);
                cell.setHorizontalAlignment("Qty".equals(header) ? Element.ALIGN_CENTER : ("Unit price".equals(header) || "Amount".equals(header) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
                items.addCell(cell);
            }
            for (JsonNode line : order.path("lines")) {
                int quantity = line.path("quantity").asInt();
                double unitPrice = line.path("unitPrice").asDouble();
                items.addCell(bodyCell(line.path("sku").asText(""), normal, Element.ALIGN_LEFT));
                items.addCell(bodyCell(line.path("productName").asText(""), normal, Element.ALIGN_LEFT));
                items.addCell(bodyCell(String.valueOf(quantity), normal, Element.ALIGN_CENTER));
                items.addCell(bodyCell(money(unitPrice), normal, Element.ALIGN_RIGHT));
                items.addCell(bodyCell(money(unitPrice * quantity), normal, Element.ALIGN_RIGHT));
            }
            document.add(items);

            document.add(new Paragraph(" "));
            Paragraph totalLine = new Paragraph("Total: " + money(order.path("totalAmount").asDouble()), total);
            totalLine.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalLine);

            String status = order.path("status").asText("");
            if (Set.of("CANCELLED", "RETURNED", "REFUNDED").contains(status)) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Note: this order is " + status.toLowerCase()
                        + (status.equals("REFUNDED") ? " and has been refunded in full." : "."), muted));
            }
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for shopping with My E-Shop.", muted));
            document.close();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Could not generate the invoice PDF: " + ex.getMessage(), ex);
        }
        return out.toByteArray();
    }

    private PdfPCell infoCell(String label, String value, Font labelFont, Font valueFont) {
        Phrase phrase = new Phrase();
        phrase.add(new com.lowagie.text.Chunk(label + "\n", labelFont));
        phrase.add(new com.lowagie.text.Chunk(value, valueFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorderColor(LINE);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(LINE);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private String money(double amount) {
        return String.format("$%.2f", amount);
    }

    private String formatDate(String iso) {
        try {
            return LocalDateTime.parse(iso).format(DATE_TIME);
        } catch (RuntimeException ex) {
            return iso.isBlank() ? "-" : iso;
        }
    }
}