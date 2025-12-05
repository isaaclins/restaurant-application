package com.restaurant.receipt.service;

import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * ESC/POS command generator for thermal receipt printers
 * 
 * ESC/POS is the standard command language for thermal printers like Epson,
 * Star, etc.
 */
@Service
@Slf4j
public class EscPosService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ESC/POS Commands
    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;
    private static final byte LF = 0x0A;

    // Initialize printer
    private static final byte[] INIT = { ESC, '@' };

    // Text formatting
    private static final byte[] ALIGN_LEFT = { ESC, 'a', 0 };
    private static final byte[] ALIGN_CENTER = { ESC, 'a', 1 };
    private static final byte[] ALIGN_RIGHT = { ESC, 'a', 2 };

    private static final byte[] BOLD_ON = { ESC, 'E', 1 };
    private static final byte[] BOLD_OFF = { ESC, 'E', 0 };

    private static final byte[] DOUBLE_HEIGHT = { ESC, '!', 0x10 };
    private static final byte[] DOUBLE_WIDTH = { ESC, '!', 0x20 };
    private static final byte[] DOUBLE_SIZE = { ESC, '!', 0x30 };
    private static final byte[] NORMAL_SIZE = { ESC, '!', 0 };

    // Paper operations
    private static final byte[] CUT_PAPER = { GS, 'V', 66, 0 };
    private static final byte[] FEED_LINES = { ESC, 'd' }; // followed by number of lines

    // Standard thermal paper is 80mm (about 48 chars at standard width)
    private static final int LINE_WIDTH = 48;

    /**
     * Generate ESC/POS commands for a receipt
     */
    public byte[] generateEscPos(Receipt receipt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Initialize printer
            baos.write(INIT);

            // Restaurant Header (centered, bold, double height)
            baos.write(ALIGN_CENTER);
            baos.write(DOUBLE_SIZE);
            baos.write(BOLD_ON);
            writeLine(baos, receipt.getRestaurantName());
            baos.write(NORMAL_SIZE);
            baos.write(BOLD_OFF);

            // Restaurant details
            writeLine(baos, receipt.getRestaurantAddress());
            writeLine(baos, "Tel: " + receipt.getRestaurantPhone());
            if (receipt.getVatNumber() != null) {
                writeLine(baos, "UID: " + receipt.getVatNumber());
            }
            writeLine(baos, "");

            // Divider line
            baos.write(ALIGN_LEFT);
            writeLine(baos, repeatChar('-', LINE_WIDTH));

            // Receipt header
            baos.write(ALIGN_CENTER);
            baos.write(BOLD_ON);
            writeLine(baos, "QUITTUNG / RECEIPT");
            baos.write(BOLD_OFF);
            writeLine(baos, "");

            // Receipt info
            baos.write(ALIGN_LEFT);
            writeLine(baos, formatRow("Quittungsnr.:", receipt.getReceiptNumber()));
            writeLine(baos, formatRow("Bestellnr.:", receipt.getOrderNumber()));
            writeLine(baos, formatRow("Datum:", receipt.getCreatedAt().format(DATE_FORMATTER)));
            writeLine(baos, formatRow("Bestellart:", translateOrderType(receipt.getOrderType().name())));
            writeLine(baos, formatRow("Zahlung:", receipt.getPaymentMethod()));
            writeLine(baos, "");

            // Customer info
            if (receipt.getCustomerName() != null) {
                baos.write(BOLD_ON);
                writeLine(baos, "Kunde:");
                baos.write(BOLD_OFF);
                writeLine(baos, receipt.getCustomerName());
                if (receipt.getCustomerPhone() != null) {
                    writeLine(baos, receipt.getCustomerPhone());
                }
                writeLine(baos, "");
            }

            // Items header
            writeLine(baos, repeatChar('-', LINE_WIDTH));
            baos.write(BOLD_ON);
            writeLine(baos, formatItemHeader());
            baos.write(BOLD_OFF);
            writeLine(baos, repeatChar('-', LINE_WIDTH));

            // Items
            for (ReceiptItem item : receipt.getItems()) {
                writeLine(baos, formatItemLine(item));
                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    writeLine(baos, "  > " + item.getNotes());
                }
            }

            writeLine(baos, repeatChar('-', LINE_WIDTH));
            writeLine(baos, "");

            // Totals
            baos.write(ALIGN_RIGHT);
            String currency = receipt.getCurrency() != null ? receipt.getCurrency() : "CHF";
            writeLine(baos, formatRow("Zwischensumme:", formatMoney(receipt.getSubtotal(), currency)));

            if (receipt.getVatAmount() != null && receipt.getVatAmount().compareTo(BigDecimal.ZERO) > 0) {
                writeLine(baos, formatRow("MwSt. (" + receipt.getVatRate() + "%):",
                        formatMoney(receipt.getVatAmount(), currency)));
            }

            if (receipt.getDeliveryFee() != null && receipt.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                writeLine(baos, formatRow("Liefergebühr:", formatMoney(receipt.getDeliveryFee(), currency)));
            }

            if (receipt.getDiscount() != null && receipt.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                writeLine(baos, formatRow("Rabatt:", "-" + formatMoney(receipt.getDiscount(), currency)));
            }

            writeLine(baos, repeatChar('=', 30));
            baos.write(BOLD_ON);
            baos.write(DOUBLE_HEIGHT);
            writeLine(baos, formatRow("TOTAL:", formatMoney(receipt.getTotalAmount(), currency)));
            baos.write(NORMAL_SIZE);
            baos.write(BOLD_OFF);
            writeLine(baos, "");

            // Footer
            baos.write(ALIGN_CENTER);
            writeLine(baos, repeatChar('-', LINE_WIDTH));
            writeLine(baos, "Vielen Dank für Ihren Besuch!");
            writeLine(baos, "Thank you for your visit!");
            writeLine(baos, "");
            writeLine(baos, repeatChar('-', LINE_WIDTH));

            // Feed paper and cut
            baos.write(FEED_LINES);
            baos.write((byte) 5); // 5 lines
            baos.write(CUT_PAPER);

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating ESC/POS: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating ESC/POS: " + e.getMessage(), e);
        }
    }

    /**
     * Generate ESC/POS for kitchen display (larger text, no payment info)
     */
    public byte[] generateKitchenTicket(Receipt receipt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            baos.write(INIT);

            // Order number big and bold
            baos.write(ALIGN_CENTER);
            baos.write(DOUBLE_SIZE);
            baos.write(BOLD_ON);
            writeLine(baos, "BESTELLUNG #" + receipt.getOrderNumber());
            baos.write(NORMAL_SIZE);
            baos.write(BOLD_OFF);
            writeLine(baos, "");

            // Time and type
            writeLine(baos, receipt.getCreatedAt().format(DATE_FORMATTER));
            baos.write(BOLD_ON);
            writeLine(baos, translateOrderType(receipt.getOrderType().name()));
            baos.write(BOLD_OFF);
            writeLine(baos, "");

            // Customer name
            if (receipt.getCustomerName() != null) {
                baos.write(DOUBLE_HEIGHT);
                writeLine(baos, receipt.getCustomerName());
                baos.write(NORMAL_SIZE);
            }

            writeLine(baos, repeatChar('=', LINE_WIDTH));

            // Items - larger for kitchen
            baos.write(ALIGN_LEFT);
            for (ReceiptItem item : receipt.getItems()) {
                baos.write(BOLD_ON);
                baos.write(DOUBLE_HEIGHT);
                writeLine(baos, item.getQuantity() + "x " + item.getProductName());
                baos.write(NORMAL_SIZE);
                baos.write(BOLD_OFF);

                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    baos.write(BOLD_ON);
                    writeLine(baos, "   >> " + item.getNotes().toUpperCase());
                    baos.write(BOLD_OFF);
                }
                writeLine(baos, "");
            }

            writeLine(baos, repeatChar('=', LINE_WIDTH));

            // Feed and cut
            baos.write(FEED_LINES);
            baos.write((byte) 3);
            baos.write(CUT_PAPER);

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating kitchen ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating kitchen ticket: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private void writeLine(ByteArrayOutputStream baos, String text) throws IOException {
        baos.write(text.getBytes(StandardCharsets.UTF_8));
        baos.write(LF);
    }

    private String formatRow(String label, String value) {
        int spaces = LINE_WIDTH - label.length() - value.length();
        if (spaces < 1)
            spaces = 1;
        return label + repeatChar(' ', spaces) + value;
    }

    private String formatItemHeader() {
        return String.format("%-24s %5s %8s %8s", "Artikel", "Menge", "Preis", "Total");
    }

    private String formatItemLine(ReceiptItem item) {
        String name = item.getProductName();
        if (name.length() > 24) {
            name = name.substring(0, 21) + "...";
        }
        return String.format("%-24s %5d %8s %8s",
                name,
                item.getQuantity(),
                formatMoney(item.getUnitPrice(), ""),
                formatMoney(item.getTotalPrice(), ""));
    }

    private String formatMoney(BigDecimal amount, String currency) {
        if (amount == null)
            return "0.00";
        String formatted = String.format("%.2f", amount);
        return currency.isEmpty() ? formatted : currency + " " + formatted;
    }

    private String repeatChar(char c, int count) {
        return String.valueOf(c).repeat(Math.max(0, count));
    }

    private String translateOrderType(String orderType) {
        return switch (orderType.toUpperCase()) {
            case "DELIVERY" -> "LIEFERUNG / DELIVERY";
            case "PICKUP" -> "ABHOLUNG / PICKUP";
            case "DINE_IN" -> "IM LOKAL / DINE-IN";
            default -> orderType;
        };
    }
}
