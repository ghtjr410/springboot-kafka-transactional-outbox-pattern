package com.ghtjr.post.producer;

import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.service.CompensationService;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {
    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final CompensationService compensationService;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${post.event.topic.name}")
    private String topicName;

    public void publishEvent(String key, PostCreatedEvent event, OutboxEvent outboxEvent) {
//        if (true) {
//            throw new RuntimeException("Simulated exception during Kafka send for testing.");
//        }
        kafkaTemplate.send(topicName, key, event).whenComplete((result, ex) -> {
            if (ex == null) {
                // 메시지 전송 성공 시 처리
                log.info("Sent message = [{}] with offset=[{}]", event, result.getRecordMetadata().offset());
                handleSuccess(outboxEvent);
            } else {
                // 메시지 전송 실패 시 보상 트랜잭션 실행
                log.warn("Kafka send failed, retrying... Reason: {}", ex.getMessage());
                handleFailure(outboxEvent);
            }
        });
    }

    private void handleSuccess(OutboxEvent outboxEvent) {
        outboxEvent.setProcessed(true);
        outboxEvent.setSagaStatus(SagaStatus.COMPLETED);
        outboxEventRepository.save(outboxEvent);  // 이벤트 상태를 저장
        log.info("Event successfully processed and marked as COMPLETED.");
    }

    private void handleFailure(OutboxEvent outboxEvent) {
        log.info("Executing compensation transaction for eventId: {}", outboxEvent.getEventId());
        try {
            compensationService.compensateOutboxEvent(outboxEvent);  // 보상 트랜잭션 호출
            log.info("Compensation transaction completed for eventId: {}", outboxEvent.getEventId());
        } catch (Exception compensationEx) {
            log.error("Failed to compensate outbox event: {}", compensationEx.getMessage());
        }
    }

}
