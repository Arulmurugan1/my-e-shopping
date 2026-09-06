package com.myeshopping.pickingservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class PickingRequest { @NotNull @Positive private Long orderId; @Positive private int itemCount; }
