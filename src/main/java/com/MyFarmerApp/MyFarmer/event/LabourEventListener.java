package com.MyFarmerApp.MyFarmer.event;

import com.MyFarmerApp.MyFarmer.service.KafkaProducerService;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener that catches LabourEvents after a successful database commit.
 * It then dispatches the events to Kafka in a background thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LabourEventListener {

    private final KafkaProducerService kafkaProducerService;

    @Async
    @TransactionalEventListener
    public void handleLabourEvent(LabourEvent event) {
        try {
            log.info("📢 Processing Async Labour Event: {} for {}", event.getEventType(), event.getLabourName());
            
            String jsonEntry = EventPayload.labourJson(
                    event.getEventType(),
                    event.getUsername(),
                    event.getRole(),
                    event.isSuccess(),
                    event.getMessage(),
                    event.getLabourId(),
                    event.getLabourName(),
                    event.getDailyWage(),
                    event.getMobile(),
                    event.getDate(),
                    event.getPresentDays(),
                    event.getManualDays(),
                    event.getAmount(),
                    event.getMonth(),
                    event.getYear()
            );

            kafkaProducerService.sendToTopic(event.getTopic(), jsonEntry);
            
        } catch (Exception e) {
            log.error("❌ Failed to dispatch async Kafka event", e);
        }
    }
}
