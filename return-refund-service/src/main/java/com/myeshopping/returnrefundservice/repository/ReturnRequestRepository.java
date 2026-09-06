package com.myeshopping.returnrefundservice.repository;
import com.myeshopping.returnrefundservice.entity.ReturnRequestEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ReturnRequestRepository extends JpaRepository<ReturnRequestEntity,Long>{Optional<ReturnRequestEntity> findByOrderId(Long orderId);}
