package com.myeshopping.customerservice.service;

import com.myeshopping.customerservice.dto.AddressRequest;
import com.myeshopping.customerservice.dto.CustomerProfileRequest;
import com.myeshopping.customerservice.entity.CustomerAddress;
import com.myeshopping.customerservice.entity.CustomerProfile;
import com.myeshopping.customerservice.repository.CustomerAddressRepository;
import com.myeshopping.customerservice.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Transactional
    public CustomerProfile createOrUpdateProfile(CustomerProfileRequest request) {
        Optional<CustomerProfile> existing = customerProfileRepository.findByUserId(request.getUserId());

        CustomerProfile profile = existing.orElseGet(CustomerProfile::new);
        profile.setUserId(request.getUserId());
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            profile.setFirstName(request.getFirstName());
        } else if (profile.getFirstName() == null) {
            profile.setFirstName("Customer");
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            profile.setLastName(request.getLastName());
        } else if (profile.getLastName() == null) {
            profile.setLastName("");
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        return customerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<CustomerProfile> getAllProfiles() {
        return customerProfileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomerProfile getProfileByUserId(String userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found for userId: " + userId));
    }

    @Transactional
    public CustomerAddress addAddress(String userId, AddressRequest request) {
        CustomerProfile profile = getProfileByUserId(userId);

        if (request.isPrimary()) {
            List<CustomerAddress> existingPrimary = customerAddressRepository.findByCustomerIdAndPrimaryTrue(profile.getId());
            existingPrimary.forEach(address -> {
                address.setPrimary(false);
                customerAddressRepository.save(address);
            });
        }

        CustomerAddress address = CustomerAddress.builder()
                .customerId(profile.getId())
                .addressType(request.getAddressType())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .primary(request.isPrimary())
                .build();

        return customerAddressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddress> getAddresses(String userId) {
        CustomerProfile profile = getProfileByUserId(userId);
        return customerAddressRepository.findByCustomerId(profile.getId());
    }

    public CustomerProfileDashboard getDashboard(String userId) {
        CustomerProfile profile = getProfileByUserId(userId);
        List<CustomerAddress> addresses = getAddresses(userId);
        return CustomerProfileDashboard.builder()
                .customerId(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFirstName() + " " + profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .addresses(addresses)
                .build();
    }
}
