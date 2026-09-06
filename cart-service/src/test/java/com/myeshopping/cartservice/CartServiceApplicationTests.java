package com.myeshopping.cartservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.cartservice.dto.AddCartItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartServiceApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void cartLifecycleAndCheckoutShouldWork() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(42L);
        request.setSku("SKU-42");
        request.setProductName("Desk Lamp");
        request.setUnitPrice(19.50);
        request.setQuantity(2);

        mockMvc.perform(post("/api/v1/cart/1001/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(39.0))
                .andExpect(jsonPath("$.data.totalItems").value(2));

        mockMvc.perform(post("/api/v1/cart/1001/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true));

        mockMvc.perform(delete("/api/v1/cart/1001/items/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }
}
