package com.ghtjr.projection.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.projection.event.CompensationEvent;
import com.ghtjr.projection.event.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent(String key, String payload) {

        try {
            // 원본 payload에서 필요한 데이터 추출
            PostCreatedEvent event = objectMapper.readValue(payload, PostCreatedEvent.class);

            // 보상 이벤트 생성
            CompensationEvent compensationEvent = new CompensationEvent();
            compensationEvent.setEventId(key);
            compensationEvent.setPostId(event.getPostId());
            compensationEvent.setReason("이벤트 처리 중 오류 발생");

            // 보상 이벤트를 JSON으로 직렬화
            String compensationPayload = objectMapper.writeValueAsString(compensationEvent);

            // 보상 이벤트 발행
            kafkaTemplate.send("compensation-topic", key, compensationPayload);
            log.info("보상 이벤트 발행 완료: eventId={}", key);
        } catch (Exception e) {
            log.error("보상 이벤트 발행 중 오류 발생: key={}, error={}", key, e.getMessage());
            // 보상 이벤트 발행 실패 시 추가적인 처리 로직 필요
        }
    }
}
