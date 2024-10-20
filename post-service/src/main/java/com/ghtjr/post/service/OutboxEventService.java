package com.ghtjr.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.producer.PostEventProducer;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PostEventProducer messagePublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 1000)
    public void processOutboxEvents() {
        List<OutboxEvent> unprocessedEvents = outboxEventRepository.findByProcessedFalseAndSagaStatus(SagaStatus.CREATED);
        log.info("Unprocessed outbox events count: {}", unprocessedEvents.size());

        unprocessedEvents.forEach(event -> {
            try {
                // Saga 상태를 PROCESSING으로 변경
                event.setSagaStatus(SagaStatus.PROCESSING);
                outboxEventRepository.save(event);

                // payload를 PostCreatedEvent 객체로 변환
                PostCreatedEvent postCreatedEvent = objectMapper.readValue(event.getPayload(), PostCreatedEvent.class);

                // 메시지 발행
                messagePublisher.publishEvent(event.getEventId(), postCreatedEvent);

                // 상태 업데이트
                event.setProcessed(true);
                event.setSagaStatus(SagaStatus.COMPLETED);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Error publishing event: {}", e.getMessage());
                // 보상 트랜잭션 수행 또는 에러 처리 로직 추가
                event.setSagaStatus(SagaStatus.FAILED);
                outboxEventRepository.save(event);
            }
        });
    }

}
