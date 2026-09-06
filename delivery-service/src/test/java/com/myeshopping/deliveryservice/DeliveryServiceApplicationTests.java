package com.myeshopping.deliveryservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.deliveryservice.dto.DeliveryRequest;
import com.myeshopping.deliveryservice.dto.TransitionRequest;
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
@SpringBootTest @AutoConfigureMockMvc class DeliveryServiceApplicationTests {
 @Autowired MockMvc mockMvc; @Autowired ObjectMapper mapper;
 @Test void deliveryProgressionShouldWork() throws Exception {
  DeliveryRequest request = new DeliveryRequest(); request.setShipmentId(3001L); request.setRecipientName("Alice Johnson");
  String body=mockMvc.perform(post("/api/v1/deliveries").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("CREATED")).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(body).path("data").path("id").asLong(); TransitionRequest transition=new TransitionRequest(); transition.setStatus("IN_TRANSIT");
  mockMvc.perform(post("/api/v1/deliveries/"+id+"/transition").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(transition))).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));
  mockMvc.perform(get("/api/v1/deliveries/shipment/3001")).andExpect(status().isOk()).andExpect(jsonPath("$.data.shipmentId").value(3001));
 }
}
