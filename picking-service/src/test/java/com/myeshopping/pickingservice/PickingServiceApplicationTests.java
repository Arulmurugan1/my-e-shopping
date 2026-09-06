package com.myeshopping.pickingservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.pickingservice.dto.PickingRequest;
import com.myeshopping.pickingservice.dto.TransitionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest @AutoConfigureMockMvc class PickingServiceApplicationTests {
 @Autowired MockMvc mockMvc; @Autowired ObjectMapper mapper;
 @Test void pickingLifecycleShouldWork() throws Exception {
  PickingRequest request = new PickingRequest(); request.setOrderId(2001L); request.setItemCount(3);
  String body = mockMvc.perform(post("/api/v1/picking").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDING")).andReturn().getResponse().getContentAsString();
  long id = mapper.readTree(body).path("data").path("id").asLong(); TransitionRequest transition = new TransitionRequest(); transition.setStatus("IN_PROGRESS");
  mockMvc.perform(post("/api/v1/picking/" + id + "/transition").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(transition))).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
 }
}
