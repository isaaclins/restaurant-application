package com.restaurant.receipt.controller;

import com.restaurant.receipt.dto.CreateReceiptRequest;
import com.restaurant.receipt.dto.DailyReportResponse;
import com.restaurant.receipt.dto.ReceiptResponse;
import com.restaurant.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Receipt Controller - REST API for receipt management
 */
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Receipt management and PDF generation")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    @Operation(summary = "Create a new receipt")
    public ResponseEntity<ReceiptResponse> createReceipt(@Valid @RequestBody CreateReceiptRequest request) {
        ReceiptResponse receipt = receiptService.createReceipt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receipt by ID")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.getReceiptById(id));
    }

    @GetMapping("/number/{receiptNumber}")
    @Operation(summary = "Get receipt by receipt number")
    public ResponseEntity<ReceiptResponse> getReceiptByNumber(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(receiptService.getReceiptByNumber(receiptNumber));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get receipt by order ID")
    public ResponseEntity<ReceiptResponse> getReceiptByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getReceiptByOrderId(orderId));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download receipt as PDF")
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable Long id) {
        byte[] pdfData = receiptService.getReceiptPdf(id);
        ReceiptResponse receipt = receiptService.getReceiptById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + receipt.getReceiptNumber() + ".pdf");
        headers.setContentLength(pdfData.length);

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all receipts for a customer")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(receiptService.getReceiptsByCustomer(customerId));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get all receipts for a specific date")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(receiptService.getReceiptsByDate(date));
    }

    @GetMapping("/report/daily")
    @Operation(summary = "Generate daily sales report")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(receiptService.generateDailyReport(date));
    }

    @GetMapping("/report/daily/{date}")
    @Operation(summary = "Generate daily sales report for specific date")
    public ResponseEntity<DailyReportResponse> getDailyReportByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(receiptService.generateDailyReport(date));
    }
}
