package com.myeshopping.invoiceservice.controller;

import com.myeshopping.invoiceservice.dto.*;
import com.myeshopping.invoiceservice.entity.Invoice;
import com.myeshopping.invoiceservice.service.InvoiceService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceService service;
  private final com.myeshopping.invoiceservice.service.InvoicePdfService pdfService;

  @GetMapping("/orders/{orderId}/pdf")
  public ResponseEntity<byte[]> pdf(@PathVariable Long orderId) {
    com.myeshopping.invoiceservice.service.InvoicePdfService.PdfDocument doc =
      pdfService.invoicePdf(orderId);
    return ResponseEntity.ok()
      .header(
        org.springframework.http.HttpHeaders.CONTENT_TYPE,
        "application/pdf"
      )
      .header(
        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + doc.filename() + "\""
      )
      .body(doc.bytes());
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Invoice>> generate(
    @Valid @RequestBody InvoiceRequest request
  ) {
    return ok("Invoice generated successfully", service.generate(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Invoice>> get(@PathVariable Long id) {
    return ok("Invoice fetched successfully", service.get(id));
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse<Invoice>> order(
    @PathVariable Long orderId
  ) {
    return ok("Invoice fetched successfully", service.getByOrder(orderId));
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
    return ResponseEntity.ok(
      ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(Instant.now())
        .correlationId(UUID.randomUUID())
        .build()
    );
  }
}
