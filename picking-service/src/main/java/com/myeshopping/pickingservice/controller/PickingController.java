package com.myeshopping.pickingservice.controller;
import com.myeshopping.pickingservice.dto.*; import com.myeshopping.pickingservice.entity.PickingTask; import com.myeshopping.pickingservice.service.PickingService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.UUID;
@RestController @RequestMapping("/api/v1/picking") @RequiredArgsConstructor public class PickingController {
 private final PickingService service;
 @PostMapping public ResponseEntity<ApiResponse<PickingTask>> create(@Valid @RequestBody PickingRequest request){return ok("Picking task created successfully",service.create(request));}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<PickingTask>> get(@PathVariable Long id){return ok("Picking task fetched successfully",service.get(id));}
 @PostMapping("/{id}/transition") public ResponseEntity<ApiResponse<PickingTask>> transition(@PathVariable Long id,@Valid @RequestBody TransitionRequest request){return ok("Picking task transitioned successfully",service.transition(id,request.getStatus()));}
 private <T> ResponseEntity<ApiResponse<T>> ok(String message,T data){return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(Instant.now()).correlationId(UUID.randomUUID()).build());}
}
