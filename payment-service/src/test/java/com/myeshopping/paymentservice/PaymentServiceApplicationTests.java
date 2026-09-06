package com.myeshopping.paymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.paymentservice.dto.PaymentRequest;
import com.myeshopping.paymentservice.dto.PaymentStatusRequest;
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
class PaymentServiceApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void paymentIdempotencyAndRefundFlowShouldWork() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(2001L); request.setCustomerId(1001L); request.setAmount(39.0); request.setCurrency("USD");
        String key = "payment-key-2001";
        String first = mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("INITIATED"))
                .andReturn().getResponse().getContentAsString();
        long paymentId = objectMapper.readTree(first).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(paymentId));

        PaymentStatusRequest success = new PaymentStatusRequest();
        success.setStatus("SUCCESS");
        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(success)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("SUCCESS"));

        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/refund"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("REFUND_PENDING"));

        mockMvc.perform(get("/api/v1/payments/" + paymentId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderId").value(2001));
    }
}
