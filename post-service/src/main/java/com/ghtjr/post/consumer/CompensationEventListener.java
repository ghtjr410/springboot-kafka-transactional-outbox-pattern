package com.ghtjr.post.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.event.PostCompensationEvent;
import com.ghtjr.post.model.Outbox;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.repository.OutboxRepository;
import com.ghtjr.post.repository.PostRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationEventListener {

    private final PostRepository postRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "compensation-topic", groupId = "post-service-group")
    @Transactional
    public void handleCompensationEvent(ConsumerRecord<String, String> record) {
        try {
            log.info("Received Kafka record: {}", record.value());
            String payload = record.value();

            log.info("Deserializing payload: {}", payload);
            PostCompensationEvent event = objectMapper.readValue(payload, PostCompensationEvent.class);

            // 보상 트랜잭션 수행 (게시글 삭제)
            log.info("Processing compensation event: {}", event);
            Post post = postRepository.findByUuid(event.getPostId());
            if (post != null) {
                log.info("Post found with uuid: {}, proceeding with deletion.", event.getPostId());
                postRepository.delete(post);
                log.info("Post with uuid: {} deleted successfully.", event.getPostId());
            }

            Outbox outbox = outboxRepository.findByAggregateId(event.getPostId());
            if(outbox != null) {
                outbox.setSagaStatus(SagaStatus.COMPENSATED);
                outboxRepository.save(outbox);
                log.info("Outbox event with uuid: {} updated to COMPENSATED.", event.getPostId());
            }
        } catch (Exception e) {
            log.error("Error processing compensation event", e);
        }
    }
}
