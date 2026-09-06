package com.myeshopping.invoiceservice.repository;
import com.myeshopping.invoiceservice.entity.Invoice; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface InvoiceRepository extends JpaRepository<Invoice,Long>{Optional<Invoice> findByOrderId(Long orderId);}
