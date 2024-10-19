package com.ghtjr.projection.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.projection.event.CompensationEvent;
import com.ghtjr.projection.event.PostCreatedEvent;
import com.ghtjr.projection.model.Post;
import com.ghtjr.projection.model.ProcessedEvent;
import com.ghtjr.projection.repository.PostRepository;
import com.ghtjr.projection.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.core.KafkaTemplate;
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
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message: key={}, value={}", record.key(), record.value());

        String eventId = record.key();
        String payload = record.value();

        // 멱등성 체크
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("이미 처리된 이벤트입니다. eventId={}", eventId);
            ack.acknowledge();
            return;
        }

        ProcessedEvent processedEvent = new ProcessedEvent();
        try {
            // 처리 중 상태 저장
            processedEvent.setEventId(eventId);
            processedEvent.setStatus("PROCESSING");
            processedEventRepository.save(processedEvent);

//            if(true){
//                throw new RuntimeException("의도적인 테스트 예외 발생");
//            }


            // 이벤트 처리 로직
            PostCreatedEvent event = objectMapper.readValue(payload, PostCreatedEvent.class);
            Post post = new Post();
            post.setUuid(event.getPostId());
            post.setUserUuid(event.getUserUuid());
            post.setNickname(event.getNickname());
            post.setTitle(event.getTitle());
            post.setContent(event.getContent());
            post.setCreatedDate(event.getCreatedDate());
            post.setUpdatedDate(event.getUpdatedDate());

            // MongoDB에 저장 (업서트)
            postRepository.save(post);
            log.info("Post 저장 완료: uuid={}", post.getUuid());

            // 처리 완료 상태 업데이트
            processedEvent.setStatus("COMPLETED");
            processedEventRepository.save(processedEvent);

            // 수동 커밋
            ack.acknowledge();

        } catch (Exception e) {
            log.error("이벤트 처리 중 오류 발생: eventId={}, error={}", eventId, e.getMessage());

            // 보상 이벤트 발행
            publishCompensationEvent(eventId, payload);
            // 처리 실패 상태 업데이트
            processedEvent.setStatus("FAILED");
            processedEventRepository.save(processedEvent);

            // 수동 커밋하여 재처리를 방지
            ack.acknowledge();
        }
    }

    private void publishCompensationEvent(String key, String payload) {
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

        } catch (Exception ex) {
            log.error("보상 이벤트 발행 중 오류 발생: key={}, error={}", key, ex.getMessage());
            // 보상 이벤트 발행 실패 시 추가적인 처리 로직 필요
        }
    }
}
