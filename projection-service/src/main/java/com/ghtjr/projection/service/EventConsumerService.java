package com.ghtjr.projection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.projection.event.CompensationEvent;
import com.ghtjr.projection.model.Post;
import com.ghtjr.projection.model.ProcessedEvent;
import com.ghtjr.projection.repository.PostRepository;
import com.ghtjr.projection.repository.ProcessedEventRepository;

import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {
    private final PostRepository postRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "${projection.topic.name}", groupId = "${projection.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message: {}", record);

        String eventId = record.key();
        String payload = record.value();

        // 멱등성 체크
        if (processedEventRepository.existsByEventId(eventId)) {
            // 이미 처리된 이벤트이므로 커밋하고 종료
            System.out.println("이미 처리된 이벤트 이므로 커밋하고 종료");
            ack.acknowledge();
            return;
        }

        try {
            // 처리 중 상태 저장
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEvent.setStatus("PROCESSING");
            processedEventRepository.save(processedEvent);

            // 이벤트 처리 로직
            log.info("payload를 Post 객체로 변환합니다.");
            Post post = objectMapper.readValue(payload, Post.class);

//            // 인위적으로 예외 발생
//            if (true) {
//                throw new RuntimeException("인위적인 예외 발생");
//            }

            // MongoDB에 저장 (업서트 사용)
            postRepository.save(post);

            // 처리 완료 상태 업데이트
            processedEvent.setStatus("COMPLETED");
            processedEventRepository.save(processedEvent);

            // 수동 커밋
            ack.acknowledge();

        } catch (DuplicateKeyException e) {
            // 중복 키 예외 발생 시 이미 처리된 것으로 간주하고 커밋
            log.warn("중복된 데이터입니다. 이벤트를 커밋합니다.", e);
            ack.acknowledge();
        } catch (Exception e) {
            // 보상 이벤트 발행
            publishCompensationEvent(eventId, payload);

            // 에러 로그 남기기
            log.error("이벤트 처리 중 오류 발생, 보상 이벤트를 발행합니다.", e);

            // 수동 커밋하여 재처리를 방지
            ack.acknowledge();
        }
    }

    private void publishCompensationEvent(String key, String payload) {
        try {
            // 원본 payload에서 필요한 데이터 추출
            Post post = objectMapper.readValue(payload, Post.class);

            // 보상 이벤트 생성
            CompensationEvent compensationEvent = new CompensationEvent();
            compensationEvent.setPostId(post.getUuid()); // 또는 적절한 ID 필드 사용
            compensationEvent.setReason("이벤트 처리 중 오류 발생");

            // 보상 이벤트를 JSON으로 직렬화
            String compensationPayload = objectMapper.writeValueAsString(compensationEvent);

            // 보상 이벤트 발행
            kafkaTemplate.send("compensation-topic", key, compensationPayload);

        } catch (Exception ex) {
            log.error("보상 이벤트 발행 중 오류 발생", ex);
            // 보상 이벤트 발행 실패 시 추가적인 처리 로직이 필요할 수 있습니다.
        }
    }
}
