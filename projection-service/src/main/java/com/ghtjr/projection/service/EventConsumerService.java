package com.ghtjr.projection.service;

import com.ghtjr.projection.model.ProcessedEvent;
import com.ghtjr.projection.repository.ProcessedEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventConsumerService {
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "${projection.topic.name}", groupId = "${projection.consumer.group-id}")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {

        String eventId = record.key();
        String payload = record.value();

        // 멱등성 체크
        if (processedEventRepository.existsById(eventId)) {
            // 이미 처리된 이벤트이므로 커밋하고 종료
            ack.acknowledge();
            return;
        }

        try {
            // 이벤트 처리 로직 (여기서는 콘솔 출력)
            System.out.println("Received event: " + payload);

            // 테스트를 위해 예외 발생
            if (true) {
                throw new RuntimeException("Processing failed");
            }

            // 처리된 이벤트 저장
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEventRepository.save(processedEvent);

            // 수동 커밋
            ack.acknowledge();

        } catch (Exception e) {
            // 예외 발생 시 롤백되고 재처리됨
            throw e;
        }
    }
}
