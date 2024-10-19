package com.ghtjr.projection.service;

import com.ghtjr.projection.event.CompensationEvent;
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
public class CompensationService {

    private final PostRepository postRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processCompensation(CompensationEvent event) {
        String eventId = event.getEventId();
        String postId = event.getPostId();
        String reason = event.getReason();

        log.info("Processing compensation for eventId: {} and postId: {}", eventId, postId);

        // Idempotency 체크
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("Compensation event already processed: eventId={}, postId={}", eventId, postId);
            return;
        }

        // 처리 중으로 상태 업데이트
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setStatus("PROCESSING");
        processedEventRepository.save(processedEvent);

        try {
            // 게시글 삭제
            Post post = postRepository.findByUuid(postId);
            if (post != null) {
                postRepository.delete(post);
                log.info("Post with uuid: {} deleted successfully.", postId);
            }

            // 처리 완료 상태로 업데이트
            processedEvent.setStatus("COMPLETED");
            processedEventRepository.save(processedEvent);
        } catch (Exception e) {
            log.error("Error during compensation processing for eventId: {}", eventId, e);
            throw e;  // 롤백 처리
        }
    }
}
