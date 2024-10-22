package com.ghtjr.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.producer.PostEventProducer;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final PostEventProducer postEventProducer;
    private final ObjectMapper objectMapper;
    private final OutboxEventProcessor outboxEventProcessor;

    @Scheduled(fixedRate = 1000)
    public void processOutboxEvents() {
        List<OutboxEvent> unprocessedEvents = outboxEventRepository.findByProcessedFalseAndSagaStatus(SagaStatus.CREATED);
        log.info("Unprocessed outbox events count: {}", unprocessedEvents.size());

        unprocessedEvents.forEach(event -> {
            try {
                outboxEventProcessor.processEventWithRetry(event);
            } catch (Exception e) {
                log.error("Error processing event after retries: {}", e.getMessage());
            }
        });
    }
}
