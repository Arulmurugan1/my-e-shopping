package com.myeshopping.customerservice.repository;

import com.myeshopping.customerservice.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerId(Long customerId);
    List<CustomerAddress> findByCustomerIdAndPrimaryTrue(Long customerId);
}
