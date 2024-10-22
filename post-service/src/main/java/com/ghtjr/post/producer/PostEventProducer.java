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

    public void publishEvent(String key, PostCreatedEvent event) throws Exception {
        try {
            // 예외를 발생시켜 재시도 동작을 확인
            if(true){
                throw new RuntimeException("Simulated exception for testing");
            }
            // 메시지 전송 및 결과 확인 (동기적으로 처리)
            SendResult<String, PostCreatedEvent> result = kafkaTemplate.send(topicName, key, event).get();
            log.info("Sent message = [{}] with offset=[{}]", event, result.getRecordMetadata().offset());
        } catch (Exception ex) {
            // 전송 실패 시 예외를 던짐
            log.error("Failed to send message: {}", ex.getMessage());
            throw ex;
        }
    }
}
