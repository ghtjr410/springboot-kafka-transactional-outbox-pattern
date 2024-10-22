package com.ghtjr.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.producer.PostEventProducer;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.repository.PostRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxEventRepository outboxEventRepository;
    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;
    private final ObjectMapper objectMapper;
    private final CompensationService compensationService;

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void processEventWithRetry(OutboxEvent event) throws Exception {
        // Update Saga status to PROCESSING
        event.setSagaStatus(SagaStatus.PROCESSING);
        outboxEventRepository.save(event);

        // Convert payload to PostCreatedEvent
        PostCreatedEvent postCreatedEvent = objectMapper.readValue(event.getPayload(), PostCreatedEvent.class);

        // Publish message
        postEventProducer.publishEvent(event.getEventId(), postCreatedEvent);

        // Update status to COMPLETED
        event.setProcessed(true);
        event.setSagaStatus(SagaStatus.COMPLETED);
        outboxEventRepository.save(event);
    }

    @Recover
    public void recover(Exception e, OutboxEvent event) {
        log.error("Recovering from exception for eventId: {}, postUuid: {}", event.getEventId(), event.getPostId());
        compensationService.compensateOutboxEvent(event);
    }
}
