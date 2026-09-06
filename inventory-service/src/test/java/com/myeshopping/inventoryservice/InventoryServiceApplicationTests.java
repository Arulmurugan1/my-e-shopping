package com.myeshopping.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.inventoryservice.dto.ProductRequest;
import com.myeshopping.inventoryservice.dto.StockAdjustmentRequest;
import com.myeshopping.inventoryservice.dto.StockReservationRequest;
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
class InventoryServiceApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void productStockAndReservationFlowShouldWork() throws Exception {
        ProductRequest product = new ProductRequest();
        product.setSku("SKU-100");
        product.setName("Wireless Keyboard");
        product.setDescription("Compact keyboard");
        product.setPrice(49.99);
        product.setInitialStock(10);

        String response = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        long productId = objectMapper.readTree(response).path("data").path("id").asLong();

        StockReservationRequest reservation = new StockReservationRequest();
        reservation.setQuantity(4);
        mockMvc.perform(post("/api/v1/inventory/" + productId + "/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(6));

        StockAdjustmentRequest adjustment = new StockAdjustmentRequest();
        adjustment.setQuantity(3);
        mockMvc.perform(post("/api/v1/inventory/" + productId + "/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableQuantity").value(9));

        mockMvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-100"));
    }
}
