package com.ghtjr.post.service;

import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.util.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventProcessor outboxEventProcessor;

    // 일정 시간마다 처리되지 않은 이벤트를 가져와서 처리
    @Scheduled(fixedRate = 1000)
    public void processOutboxEvents() {
        // 처리되지 않은 이벤트들을 가져옴 (FAILED 상태의 이벤트는 처리하지 않음)
        List<OutboxEvent> unprocessedEvents = outboxEventRepository.findByProcessedFalseAndSagaStatus(SagaStatus.CREATED);
        log.info("Unprocessed outbox events count: {}", unprocessedEvents.size());

        // 각 이벤트를 처리
        unprocessedEvents.forEach(outboxEventProcessor::processEvent);
    }
}
