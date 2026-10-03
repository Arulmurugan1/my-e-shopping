package com.myeshopping.shipmentservice.controller;
import com.myeshopping.shipmentservice.dto.*; import com.myeshopping.shipmentservice.entity.Shipment; import com.myeshopping.shipmentservice.service.ShipmentService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.UUID;
@RestController @RequestMapping("/api/v1/shipments") @RequiredArgsConstructor public class ShipmentController {
 private final ShipmentService service; private final com.myeshopping.shipmentservice.service.ShipmentFulfillmentService fulfillmentService; @PostMapping("/orders/{orderId}/fulfill") public ResponseEntity<ApiResponse<Shipment>> fulfill(@PathVariable Long orderId,@RequestParam(defaultValue="MYE-Express") String carrier){return ok("Order shipped successfully",fulfillmentService.fulfill(orderId,carrier));}
 @PostMapping public ResponseEntity<ApiResponse<Shipment>> create(@Valid @RequestBody ShipmentRequest request){return ok("Shipment created successfully",service.create(request));}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<Shipment>> get(@PathVariable Long id){return ok("Shipment fetched successfully",service.get(id));}
 @GetMapping("/order/{orderId}") public ResponseEntity<ApiResponse<Shipment>> getByOrder(@PathVariable Long orderId){return ok("Shipment fetched successfully",service.getByOrder(orderId));}
 @PostMapping("/{id}/complete") public ResponseEntity<ApiResponse<Shipment>> complete(@PathVariable Long id){return ok("Shipment completed successfully",service.complete(id));}
 private <T> ResponseEntity<ApiResponse<T>> ok(String message,T data){return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(Instant.now()).correlationId(UUID.randomUUID()).build());}
}
