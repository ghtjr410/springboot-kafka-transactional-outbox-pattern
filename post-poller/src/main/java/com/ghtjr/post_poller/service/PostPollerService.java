package com.ghtjr.post_poller.service;

import com.ghtjr.post_poller.model.Outbox;
import com.ghtjr.post_poller.publisher.MessagePublisher;
import com.ghtjr.post_poller.repository.OutboxRepository;
import com.ghtjr.post_poller.util.SagaStatus;
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
public class PostPollerService {
    private final OutboxRepository outboxRepository;
    private final MessagePublisher messagePublisher;


    @Scheduled(fixedRate = 1000)
    public void pollOutboxMessagesAndPublish(){
        // 1. fetch unprocessed record
        List<Outbox> unprocessedRecords = outboxRepository.findByProcessedFalseAndSagaStatus(SagaStatus.CREATED);
        log.info("unprocessed record count : {}", unprocessedRecords.size());
        // 2. publish record to kafka/queue
        unprocessedRecords.forEach(outbox -> {
            try{
                // Saga 상태를 PROCESSING으로 변경
                outbox.setSagaStatus(SagaStatus.PROCESSING);
                outboxRepository.save(outbox);

                // 메세지 발행
                messagePublisher.publish(outbox.getAggregateId(),outbox.getPayload());
                
                // 메세지 상태 COMPLETED로 업데이트, 메세지 발행을 TRUE로 변경
                outbox.setProcessed(true);
                outbox.setSagaStatus(SagaStatus.COMPLETED);
                outboxRepository.save(outbox);
            } catch (Exception ignored) {
                log.error("Error publishing message: {}", ignored.getMessage());
                
                // 실패 시 Saga 상태를 FAILED로 업데이트
                outbox.setSagaStatus(SagaStatus.FAILED);
                outboxRepository.save(outbox);

                // 보상 트랜잭션 수행
                compensate(outbox);
            }
        });
    }
    private void compensate(Outbox outbox) {
        // 보상 로직 구현 (예: 원래 작업을 취소)
        // 여기서는 간단히 로그로 표시
        log.info("Compensating transaction for aggregateId: {}", outbox.getAggregateId());

        // Saga 상태를 COMPENSATED로 업데이트
        outbox.setSagaStatus(SagaStatus.COMPENSATED);
        outboxRepository.save(outbox);
    }
}
