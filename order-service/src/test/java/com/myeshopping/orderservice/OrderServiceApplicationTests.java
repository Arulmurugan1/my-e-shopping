package com.myeshopping.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.orderservice.dto.CreateOrderRequest;
import com.myeshopping.orderservice.dto.OrderLineRequest;
import com.myeshopping.orderservice.dto.OrderTransitionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void orderCreationAndValidTransitionsShouldWork() throws Exception {
        OrderLineRequest line = new OrderLineRequest();
        line.setProductId(42L); line.setSku("SKU-42"); line.setProductName("Desk Lamp");
        line.setUnitPrice(19.50); line.setQuantity(2);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1001L); request.setShippingAddress("123 Market St"); request.setLines(java.util.List.of(line));

        String body = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("ORDERED"))
                .andExpect(jsonPath("$.data.totalAmount").value(39.0)).andReturn().getResponse().getContentAsString();
        long orderId = objectMapper.readTree(body).path("data").path("id").asLong();

        OrderTransitionRequest transition = new OrderTransitionRequest();
        transition.setStatus("PICKING_PENDING");
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/transition")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transition)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PICKING_PENDING"));

        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.customerId").value(1001));
    }
}
