package com.myeshopping.customerservice.service;

import com.myeshopping.customerservice.entity.CustomerAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileDashboard {
    private Long customerId;
    private String userId;
    private String fullName;
    private String phoneNumber;
    private List<CustomerAddress> addresses;
}
