package com.ghtjr.post.producer;

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
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${post.event.topic.name}")
    private String topicName;

    public void publishEvent(String key, String payload) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 성공적으로 전송됨
                log.info("Sent message = [{}] with offset=[{}]", payload, result.getRecordMetadata().offset());
            } else {
                // 전송 실패 처리
                log.error("Unable to send message=[{}] due to : {}", payload, ex.getMessage());
            }
        });
    }
}
