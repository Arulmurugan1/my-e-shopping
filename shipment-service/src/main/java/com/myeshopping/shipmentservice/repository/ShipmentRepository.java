package com.myeshopping.shipmentservice.repository;
import com.myeshopping.shipmentservice.entity.Shipment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ShipmentRepository extends JpaRepository<Shipment,Long>{Optional<Shipment> findByOrderId(Long orderId);}
