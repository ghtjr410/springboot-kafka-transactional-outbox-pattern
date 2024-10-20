package com.ghtjr.post.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.service.CompensationService;
import com.ghtjr.projection.avro.PostCompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationEventConsumer {

    private final CompensationService compensationService;

    @KafkaListener(topics = "${compensation.topic.name}", groupId = "{post.consumer.group-id}")
    public void consume(ConsumerRecord<String, PostCompensationEvent> record, Acknowledgment ack) {
        try {
            log.info("Received compensation message: key={}, value={}", record.key(), record.value());

            String eventId = record.key();
            PostCompensationEvent postCompensationEvent = record.value();
            compensationService.processCompensation(eventId, postCompensationEvent);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing compensation event", e);
            ack.acknowledge();
        }
    }
}
