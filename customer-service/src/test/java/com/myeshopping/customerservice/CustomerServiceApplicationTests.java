package com.myeshopping.customerservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.customerservice.dto.CustomerProfileRequest;
import com.myeshopping.customerservice.dto.AddressRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomerAndAddressShouldWork() throws Exception {
        CustomerProfileRequest profileRequest = new CustomerProfileRequest();
        profileRequest.setUserId(1001L);
        profileRequest.setFirstName("Alice");
        profileRequest.setLastName("Johnson");
        profileRequest.setPhoneNumber("555-0101");

        mockMvc.perform(post("/api/v1/customers/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressType("HOME");
        addressRequest.setStreet("123 Market St");
        addressRequest.setCity("Seattle");
        addressRequest.setState("WA");
        addressRequest.setPostalCode("98101");
        addressRequest.setCountry("USA");
        addressRequest.setPrimary(true);

        mockMvc.perform(post("/api/v1/customers/1001/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/customers/1001/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
