package com.myeshopping.shipmentservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.shipmentservice.dto.ShipmentRequest;
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
@SpringBootTest @AutoConfigureMockMvc class ShipmentServiceApplicationTests {
 @Autowired MockMvc mockMvc; @Autowired ObjectMapper mapper;
 @Test void shipmentCreationTrackingAndCompletionShouldWork() throws Exception {
  ShipmentRequest request = new ShipmentRequest(); request.setOrderId(2001L); request.setCarrier("LOCAL_CARRIER");
  String body = mockMvc.perform(post("/api/v1/shipments").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("CREATED")).andExpect(jsonPath("$.data.trackingNumber").isNotEmpty()).andReturn().getResponse().getContentAsString();
  long id = mapper.readTree(body).path("data").path("id").asLong();
  mockMvc.perform(post("/api/v1/shipments/" + id + "/complete")).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("COMPLETED"));
  mockMvc.perform(get("/api/v1/shipments/order/2001")).andExpect(status().isOk()).andExpect(jsonPath("$.data.orderId").value(2001));
 }
}
