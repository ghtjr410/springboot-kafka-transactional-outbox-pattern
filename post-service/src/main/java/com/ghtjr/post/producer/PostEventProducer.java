package com.ghtjr.post.producer;

import com.ghtjr.post.avro.PostCreatedEvent;
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

    @Value("${post.event.topic.name}")
    private String topicName;

    public void publishEvent(String key, PostCreatedEvent event) {
        CompletableFuture<SendResult<String, PostCreatedEvent>> future = kafkaTemplate.send(topicName, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 성공적으로 전송됨
                log.info("Sent message = [{}] with offset=[{}]", event, result.getRecordMetadata().offset());
            } else {
                // 전송 실패 처리
                log.error("Unable to send message=[{}] due to : {}", event, ex.getMessage());
            }
        });
    }
}
