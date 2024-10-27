package com.ghtjr.projection.consumer;

import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.projection.service.PostCreatedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreatedEventConsumer {
    private final PostCreatedEventService postCreatedEventService;

    @KafkaListener(topics = "${post.event.topic.created}", groupId = "${projection.consumer.group-id}")
    public void consume(ConsumerRecord<String, PostCreatedEvent> record, Acknowledgment ack) {
        log.info("Received message: key={}, value={}", record.key(), record.value());

        String eventId = record.key();
        PostCreatedEvent postCreatedEvent = record.value();
        // 예외를 발생시켜 재시도 확인
//        if (true) {
//            throw new RuntimeException("컨슈머 메서드에서 강제 예외 발생");
//        }
        postCreatedEventService.processEvent(eventId, postCreatedEvent);
        ack.acknowledge();
    }
}
