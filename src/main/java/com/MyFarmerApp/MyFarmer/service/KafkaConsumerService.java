package com.MyFarmerApp.MyFarmer.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = KafkaProducerService.TOPIC, groupId = "mydairy-consumer")
    public void listen(String message) {
        System.out.println("📩 Received Kafka event: " + message);
    }
}