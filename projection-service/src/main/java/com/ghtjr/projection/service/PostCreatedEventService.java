package com.ghtjr.projection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.projection.model.Post;
import com.ghtjr.projection.model.ProcessedEvent;
import com.ghtjr.projection.repository.PostRepository;
import com.ghtjr.projection.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCreatedEventService {
    private final PostRepository postRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final PostCompensationEventService postCompensationEventService;

    @Transactional
    public void processEvent(String eventId, PostCreatedEvent postCreatedEvent) {
        log.info("processEvent called for eventId={}", eventId);
        // 여기에서 강제로 예외 발생

        // 멱등성 체크
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("이미 처리된 이벤트입니다. eventId={}", eventId);
            return;
        }
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setStatus("PROCESSING");
        processedEventRepository.save(processedEvent);
//        if(true){
//            throw new RuntimeException("강제 예외 발생으로 보상 트랜잭션 테스트");
//        }
        // 이벤트 처리 로직
        Post post = new Post();
        post.setUuid(postCreatedEvent.getPostId().toString());
        post.setUserUuid(postCreatedEvent.getUserUuid().toString());
        post.setNickname(postCreatedEvent.getNickname().toString());
        post.setTitle(postCreatedEvent.getTitle().toString());
        post.setContent(postCreatedEvent.getContent().toString());
        post.setCreatedDate(postCreatedEvent.getCreatedDate());
        post.setUpdatedDate(postCreatedEvent.getUpdatedDate());
        // MongoDB에 저장
        postRepository.save(post);
        log.info("Post 저장 완료: uuid={}", post.getUuid());
        // 처리 완료 상태 업데이트
        processedEvent.setStatus("COMPLETED");
        processedEventRepository.save(processedEvent);
    }
}
