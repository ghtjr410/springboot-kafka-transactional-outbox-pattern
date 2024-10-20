package com.ghtjr.projection.producer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ghtjr.projection.avro.PostCompensationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationEventProducer {
    private final KafkaTemplate<String, PostCompensationEvent> kafkaTemplate;

    public void publishEvent(String key, PostCompensationEvent postCompensationEvent) {
        CompletableFuture<SendResult<String, PostCompensationEvent>> future = kafkaTemplate.send("compensation-topic", key, postCompensationEvent);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("보상 이벤트 발행 완료: eventId={}", key);
            } else {
                log.error("보상 이벤트 발행 중 오류 발생: key={}, error={}", key, ex.getMessage());
            }
        });
    }
}
