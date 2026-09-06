package com.myeshopping.returnrefundservice.dto;
import jakarta.validation.constraints.NotBlank; import lombok.Data;
@Data public class TransitionRequest { @NotBlank private String status; }
