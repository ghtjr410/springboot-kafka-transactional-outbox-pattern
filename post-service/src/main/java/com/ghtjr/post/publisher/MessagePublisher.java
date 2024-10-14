package com.ghtjr.post.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MessagePublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${post.poller.topic.name}")
    private String topicName;

    public void publish(String key, String payload) {
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("eventType", "PostCreatedEvent")
                .build();

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, key, payload);
        future.whenComplete((result, ex) -> {
            if(ex == null) {
                System.out.println("Sent message = [" + payload +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send message=[" +
                        payload + "] due to : " + ex.getMessage());
            }
        });
    }
}
