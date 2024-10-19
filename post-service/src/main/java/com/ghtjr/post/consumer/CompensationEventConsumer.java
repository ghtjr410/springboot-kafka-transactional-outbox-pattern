package com.ghtjr.post.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.event.PostCompensationEvent;
import com.ghtjr.post.service.CompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationEventConsumer {

    private final CompensationService compensationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${compensation.topic.name}", groupId = "{post.consumer.group-id}")
    public void consumeCompensationEvent(String message, Acknowledgment ack) {
        try {
            log.info("Received compensation event: {}", message);
            PostCompensationEvent event = objectMapper.readValue(message, PostCompensationEvent.class);
            compensationService.processCompensation(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing compensation event", e);
            ack.acknowledge();
        }
    }
}
