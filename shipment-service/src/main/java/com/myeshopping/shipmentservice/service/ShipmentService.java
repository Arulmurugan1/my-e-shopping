package com.myeshopping.shipmentservice.service;
import com.myeshopping.shipmentservice.dto.ShipmentRequest; import com.myeshopping.shipmentservice.entity.*; import com.myeshopping.shipmentservice.repository.ShipmentRepository; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime; import java.util.UUID;
@Service @RequiredArgsConstructor public class ShipmentService {
 private final ShipmentRepository repository;
 @Transactional public Shipment create(ShipmentRequest request){if(repository.findByOrderId(request.getOrderId()).isPresent())throw new IllegalArgumentException("Shipment already exists for order: "+request.getOrderId()); return repository.save(Shipment.builder().orderId(request.getOrderId()).carrier(request.getCarrier()).trackingNumber("MYE-"+UUID.randomUUID().toString().substring(0,8).toUpperCase()).status(ShipmentStatus.CREATED).build());}
 @Transactional(readOnly=true) public Shipment get(Long id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Shipment not found: "+id));}
 @Transactional(readOnly=true) public Shipment getByOrder(Long orderId){return repository.findByOrderId(orderId).orElseThrow(()->new IllegalArgumentException("Shipment not found for order: "+orderId));}
 @Transactional public Shipment complete(Long id){Shipment shipment=get(id);if(shipment.getStatus()!=ShipmentStatus.CREATED&&shipment.getStatus()!=ShipmentStatus.IN_TRANSIT)throw new IllegalArgumentException("Shipment cannot be completed from "+shipment.getStatus());shipment.setStatus(ShipmentStatus.COMPLETED);shipment.setCompletedAt(LocalDateTime.now());return repository.save(shipment);}
}
