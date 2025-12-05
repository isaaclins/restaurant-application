package com.restaurant.receipt.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * PDF Generation Service using OpenPDF
 */
@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);

    /**
     * Generate PDF receipt
     */
    public byte[] generateReceiptPdf(Receipt receipt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Restaurant Header
            addRestaurantHeader(document, receipt);

            // Receipt Info
            addReceiptInfo(document, receipt);

            // Customer Info
            addCustomerInfo(document, receipt);

            // Order Items Table
            addItemsTable(document, receipt);

            // Pricing Summary
            addPricingSummary(document, receipt);

            // Footer
            addFooter(document, receipt);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void addRestaurantHeader(Document document, Receipt receipt) throws DocumentException {
        Paragraph title = new Paragraph(receipt.getRestaurantName(), TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph address = new Paragraph(receipt.getRestaurantAddress(), SMALL_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);

        Paragraph phone = new Paragraph("Tel: " + receipt.getRestaurantPhone(), SMALL_FONT);
        phone.setAlignment(Element.ALIGN_CENTER);
        document.add(phone);

        if (receipt.getVatNumber() != null) {
            Paragraph vat = new Paragraph("UID: " + receipt.getVatNumber(), SMALL_FONT);
            vat.setAlignment(Element.ALIGN_CENTER);
            document.add(vat);
        }

        document.add(new Paragraph(" ")); // Spacer
        document.add(new Paragraph("─".repeat(60), SMALL_FONT));
        document.add(new Paragraph(" ")); // Spacer
    }

    private void addReceiptInfo(Document document, Receipt receipt) throws DocumentException {
        Paragraph receiptHeader = new Paragraph("QUITTUNG / RECEIPT", HEADER_FONT);
        receiptHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(receiptHeader);
        document.add(new Paragraph(" "));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 1, 1 });

        addInfoCell(infoTable, "Quittungsnr.:", receipt.getReceiptNumber());
        addInfoCell(infoTable, "Bestellnr.:", receipt.getOrderNumber());
        addInfoCell(infoTable, "Datum:", receipt.getCreatedAt().format(DATE_FORMATTER));
        addInfoCell(infoTable, "Bestellart:", translateOrderType(receipt.getOrderType().name()));
        addInfoCell(infoTable, "Zahlungsart:", receipt.getPaymentMethod());

        document.add(infoTable);
        document.add(new Paragraph(" "));
    }

    private void addCustomerInfo(Document document, Receipt receipt) throws DocumentException {
        if (receipt.getCustomerName() != null) {
            document.add(new Paragraph("Kunde / Customer:", HEADER_FONT));
            document.add(new Paragraph(receipt.getCustomerName(), NORMAL_FONT));
            if (receipt.getCustomerEmail() != null) {
                document.add(new Paragraph(receipt.getCustomerEmail(), SMALL_FONT));
            }
            if (receipt.getCustomerPhone() != null) {
                document.add(new Paragraph(receipt.getCustomerPhone(), SMALL_FONT));
            }
            document.add(new Paragraph(" "));
        }
    }

    private void addItemsTable(Document document, Receipt receipt) throws DocumentException {
        document.add(new Paragraph("─".repeat(60), SMALL_FONT));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3, 1, 1.5f, 1.5f });

        // Header row
        addTableHeader(table, "Artikel / Item");
        addTableHeader(table, "Menge");
        addTableHeader(table, "Preis");
        addTableHeader(table, "Total");

        // Items
        for (ReceiptItem item : receipt.getItems()) {
            addTableCell(table, item.getProductName(), Element.ALIGN_LEFT);
            addTableCell(table, String.valueOf(item.getQuantity()), Element.ALIGN_CENTER);
            addTableCell(table, formatPrice(item.getUnitPrice()), Element.ALIGN_RIGHT);
            addTableCell(table, formatPrice(item.getTotalPrice()), Element.ALIGN_RIGHT);

            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                PdfPCell notesCell = new PdfPCell(new Phrase("  → " + item.getNotes(), SMALL_FONT));
                notesCell.setColspan(4);
                notesCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(notesCell);
            }
        }

        document.add(table);
        document.add(new Paragraph("─".repeat(60), SMALL_FONT));
        document.add(new Paragraph(" "));
    }

    private void addPricingSummary(Document document, Receipt receipt) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(summaryTable, "Zwischensumme:", formatPrice(receipt.getSubtotal()), false);

        if (receipt.getDeliveryFee() != null && receipt.getDeliveryFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Liefergebühr:", formatPrice(receipt.getDeliveryFee()), false);
        }

        if (receipt.getDiscount() != null && receipt.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Rabatt:", "-" + formatPrice(receipt.getDiscount()), false);
        }

        if (receipt.getVatAmount() != null) {
            addSummaryRow(summaryTable, "MwSt. (" + receipt.getVatRate() + "%):", formatPrice(receipt.getVatAmount()),
                    false);
        }

        addSummaryRow(summaryTable, "TOTAL:", formatPrice(receipt.getTotalAmount()), true);

        document.add(summaryTable);
        document.add(new Paragraph(" "));
    }

    private void addFooter(Document document, Receipt receipt) throws DocumentException {
        document.add(new Paragraph("─".repeat(60), SMALL_FONT));

        Paragraph thanks = new Paragraph("Vielen Dank für Ihre Bestellung!", NORMAL_FONT);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        Paragraph thanksEn = new Paragraph("Thank you for your order!", SMALL_FONT);
        thanksEn.setAlignment(Element.ALIGN_CENTER);
        document.add(thanksEn);
    }

    // Helper methods

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOTTOM);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean isBold) {
        Font font = isBold ? TOTAL_FONT : NORMAL_FONT;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(isBold ? Rectangle.TOP : Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatPrice(java.math.BigDecimal price) {
        return String.format("CHF %.2f", price);
    }

    private String translateOrderType(String type) {
        return switch (type) {
            case "DELIVERY" -> "Lieferung";
            case "PICKUP" -> "Abholung";
            case "DINE_IN" -> "Im Restaurant";
            default -> type;
        };
    }
}
