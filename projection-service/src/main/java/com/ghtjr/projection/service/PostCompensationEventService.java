package com.ghtjr.projection.service;

import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.projection.avro.PostCompensationEvent;
import com.ghtjr.projection.producer.CompensationEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCompensationEventService {
    private final CompensationEventProducer compensationEventProducer;

    public void processEvent(String eventId, PostCreatedEvent postCreatedEvent) {
        // 보상 이벤트 객체 생성
        PostCompensationEvent postCompensationEvent = new PostCompensationEvent();
        postCompensationEvent.setPostId(postCreatedEvent.getPostId());
        postCompensationEvent.setReason("이벤트 처리 중 오류 발생");
        // 보상 이벤트 발행
        compensationEventProducer.publishEvent(eventId, postCompensationEvent);
    }
}
