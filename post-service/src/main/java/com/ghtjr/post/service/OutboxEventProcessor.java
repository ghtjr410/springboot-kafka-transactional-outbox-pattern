package com.ghtjr.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.producer.PostEventProducer;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.util.SagaStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxEventRepository outboxEventRepository;
    private final PostEventProducer postEventProducer;
    private final ObjectMapper objectMapper;
    private final CompensationService compensationService;

    @Transactional
    public void processEvent(OutboxEvent outboxEvent){
        try {
            // Saga 상태를 PROCESSING으로 업데이트
            outboxEvent.setSagaStatus(SagaStatus.PROCESSING);
            outboxEventRepository.save(outboxEvent);

//            if (true) {
//                throw new RuntimeException("Simulated exception during Kafka send for testing.");
//            }
            // Payload를 PostCreatedEvent로 변환
            PostCreatedEvent postCreatedEvent = objectMapper.readValue(outboxEvent.getPayload(), PostCreatedEvent.class);
            // 메시지 발행 (Kafka에 전송)
            postEventProducer.publishEvent(outboxEvent.getEventId(), postCreatedEvent, outboxEvent);

        } catch (Exception e) {
            log.error("Error processing OutboxEvent: {}", e.getMessage());
            compensationService.compensateOutboxEvent(outboxEvent);  // 보상 트랜잭션 호출
        }
    }
}
