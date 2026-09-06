package com.myeshopping.deliveryservice.repository;
import com.myeshopping.deliveryservice.entity.Delivery; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface DeliveryRepository extends JpaRepository<Delivery,Long>{Optional<Delivery> findByShipmentId(Long shipmentId);}
