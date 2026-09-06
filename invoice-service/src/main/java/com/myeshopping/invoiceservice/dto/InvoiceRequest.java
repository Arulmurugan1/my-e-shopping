package com.myeshopping.invoiceservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class InvoiceRequest { @NotNull @Positive private Long orderId; @NotNull @Positive private Long paymentId; @NotNull @Positive private Long customerId; @Positive private double amount; @NotBlank @Size(min=3,max=3) private String currency; }
