package com.ghtjr.post.service;

import com.ghtjr.post.event.PostCompensationEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.model.ProcessedEvent;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.repository.PostRepository;
import com.ghtjr.post.repository.ProcessedEventRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompensationService {

    private final PostRepository postRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processCompensation(PostCompensationEvent event) {
        String eventId = event.getEventId();
        String postId = event.getPostId();
        log.info("Processing compensation for eventId: {} and postId: {}", eventId, postId);

        // 멱등성 체크 (이미 처리된 이벤트인지 확인)
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("이미 처리된 보상 이벤트: eventId={}, postId={}", eventId, postId);
            return; // 이미 처리된 이벤트라면 더 이상 처리하지 않음
        }

        // 처리 중 상태 저장
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId)
                .status("PROCESSING")
                .build();
        processedEventRepository.save(processedEvent);

        try {
            // 게시글 삭제
            Post post = postRepository.findByUuid(postId);
            if (post != null) {
                postRepository.delete(post);
                log.info("Post with uuid: {} deleted successfully.", postId);
            }

            // Outbox 이벤트 상태 업데이트
            OutboxEvent outboxEvent = outboxEventRepository.findByEventId(eventId);
            if (outboxEvent != null) {
                outboxEvent.setSagaStatus(SagaStatus.COMPENSATED);
                outboxEventRepository.save(outboxEvent);
                log.info("Outbox event with eventId: {} updated to COMPENSATED.", eventId);
            }

            // 처리 완료 상태 업데이트
            processedEvent.setStatus("COMPLETED");
            processedEventRepository.save(processedEvent);
        } catch (Exception e) {
            log.error("Error during compensation processing for eventId: {}", eventId, e);
            // 필요 시 롤백 또는 추가적인 에러 처리 로직 추가
            throw e; // 트랜잭션 롤백을 위해 예외 재던짐
        }
    }
}
