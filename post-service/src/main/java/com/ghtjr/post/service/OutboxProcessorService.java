package com.ghtjr.post.service;

import com.ghtjr.post.model.Outbox;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.publisher.MessagePublisher;
import com.ghtjr.post.repository.OutboxRepository;
import com.ghtjr.post.repository.PostRepository;
import com.ghtjr.post.util.SagaStatus;
import jakarta.transaction.Transactional;
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
public class OutboxProcessorService {
        private final PostRepository postRepository;
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
//                                throw new RuntimeException("Intentional exception for testing compensating transaction");
                                outboxRepository.save(outbox);
                        } catch (Exception ignored) {
                                log.error("Error publishing message: {}", ignored.getMessage());

                                // 보상 트랜잭션 수행
                                compensate(outbox);
                        }
                });
        }

        @Transactional
        private void compensate(Outbox outbox) {
                // 보상 트랜잭션 수행
                String aggregateId = outbox.getAggregateId();
                // 해당 게시글 조회
                Post post = postRepository.findByUuid(aggregateId);
                if (post != null) {
                        // 게시글 삭제 또는 상태 변경
                        postRepository.delete(post);
                        // 또는 상태 변경
                        // post.setStatus("CANCELLED");
                        // postRepository.save(post);
                }

                // Outbox의 sagaStatus를 COMPENSATED로 업데이트
                outbox.setSagaStatus(SagaStatus.COMPENSATED);
                outboxRepository.save(outbox);

                log.info("Compensated transaction for aggregateId: {}", aggregateId);
        }

}
