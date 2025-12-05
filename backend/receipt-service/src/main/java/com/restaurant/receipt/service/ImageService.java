package com.restaurant.receipt.service;

import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PNG images from receipts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final PdfService pdfService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Generate PNG image from receipt (renders PDF to image)
     */
    public byte[] generateReceiptPng(Receipt receipt) {
        try {
            // First generate PDF
            byte[] pdfBytes = pdfService.generateReceiptPdf(receipt);

            // Convert PDF to PNG
            return convertPdfToPng(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating PNG receipt: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PNG: " + e.getMessage(), e);
        }
    }

    /**
     * Convert PDF bytes to PNG image
     */
    public byte[] convertPdfToPng(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDFRenderer renderer = new PDFRenderer(document);

            // Render first page at 150 DPI
            BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.RGB);

            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error converting PDF to PNG: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting PDF to PNG: " + e.getMessage(), e);
        }
    }

    /**
     * Generate high-quality PNG for printing (300 DPI)
     */
    public byte[] generateHighQualityPng(Receipt receipt) {
        try {
            byte[] pdfBytes = pdfService.generateReceiptPdf(receipt);

            try (PDDocument document = Loader.loadPDF(pdfBytes);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                PDFRenderer renderer = new PDFRenderer(document);
                BufferedImage image = renderer.renderImageWithDPI(0, 300, ImageType.RGB);

                ImageIO.write(image, "PNG", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error generating high quality PNG: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PNG: " + e.getMessage(), e);
        }
    }
}
