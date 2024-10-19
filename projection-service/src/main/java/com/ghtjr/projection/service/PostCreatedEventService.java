package com.ghtjr.projection.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.projection.event.CompensationEvent;
import com.ghtjr.projection.event.PostCreatedEvent;
import com.ghtjr.projection.model.Post;
import com.ghtjr.projection.model.ProcessedEvent;
import com.ghtjr.projection.producer.CompensationEventProducer;
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
public class PostCreatedEventService {
    private final PostRepository postRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final CompensationEventProducer compensationEventProducer;

    @Transactional
    public void processEvent(String eventId, String payload) {
        // 멱등성 체크
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("이미 처리된 이벤트입니다. eventId={}", eventId);
            return;
        }

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setStatus("PROCESSING");
        processedEventRepository.save(processedEvent);

        try {
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

        } catch (Exception e) {
            log.error("이벤트 처리 중 오류 발생: eventId={}, error={}", eventId, e.getMessage());
            // 처리 실패 상태 업데이트
            processedEvent.setStatus("FAILED");
            processedEventRepository.save(processedEvent);

            // 보상 이벤트 발행
            compensationEventProducer.publishEvent(eventId, payload);
            throw new RuntimeException(e);
        }

    }



}
