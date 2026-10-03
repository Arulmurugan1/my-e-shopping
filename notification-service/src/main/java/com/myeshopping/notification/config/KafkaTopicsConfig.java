package com.myeshopping.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    public static final String SHIPMENT_EVENTS = "shipment-events";

    @Bean
    NewTopic shipmentEventsTopic() {
        return new NewTopic(SHIPMENT_EVENTS, 1, (short) 1);
    }
}