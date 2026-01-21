package com.MyFarmerApp.MyFarmer.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    public static final String TOPIC = "user-events";


    public static final String MILK_TOPIC = "milk-events";
    public static final String LABOUR_TOPIC = "labour-events";
    public static final String ATTENDANCE_TOPIC = "attendance-events";
    public static final String SALARY_TOPIC = "salary-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }



    public void send(String eventJson) {
        kafkaTemplate.send(TOPIC, eventJson);
        System.out.println("📤 Sent Kafka event: " + eventJson);
    }


    public void sendToTopic(String topic, String eventJson) {
        kafkaTemplate.send(topic, eventJson);
        System.out.println("📤 Sent Kafka event (" + topic + "): " + eventJson);
    }
}
