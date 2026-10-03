package com.myeshopping.orderservice.config;

import com.myeshopping.orderservice.event.DomainEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("postgres")
public class KafkaConfig {
    @Bean
    KafkaTemplate<String, DomainEvent> kafkaTemplate(org.springframework.boot.autoconfigure.kafka.KafkaProperties properties) {
        
        Map<String, Object> producerProperties = new HashMap<>(properties.buildProducerProperties());

        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProperties));
    }

    /** Retry a failed fulfilment a few times (2s apart), then log and move on so one bad message cannot block the topic. */
    @Bean
    org.springframework.kafka.listener.DefaultErrorHandler kafkaErrorHandler() {
        return new org.springframework.kafka.listener.DefaultErrorHandler(new org.springframework.util.backoff.FixedBackOff(2000L, 3L));
    }

    @Bean
    NewTopic orderEventsTopic() {
        return new NewTopic("order-events", 1, (short) 1);
    }
}
