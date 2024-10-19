package com.ghtjr.projection.consumer;

import com.ghtjr.projection.service.PostCreatedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreatedEventConsumer {
    private final PostCreatedEventService postCreatedEventService;

    @KafkaListener(topics = "${projection.topic.name}", groupId = "${projection.consumer.group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message: key={}, value={}", record.key(), record.value());

        String eventId = record.key();
        String payload = record.value();

        try {
            postCreatedEventService.processEvent(eventId, payload);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing event: eventId={}, error={}", eventId, e.getMessage());
            ack.acknowledge();
        }
    }
}
