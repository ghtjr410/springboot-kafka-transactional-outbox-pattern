package com.ghtjr.post.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${post.event.topic.name}")
    private String eventTopicName;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 재시도 설정
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // 무한 재시도
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 리플리카로부터의 ACK를 기다림
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 아이도포턴트 프로듀서 활성화

        // 추가적인 안정성 설정
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // 메시지 순서 보장


        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    // @Bean
    // public NewTopic postEventsTopic() {
    //     return new NewTopic(eventTopicName, 3, (short) 1);
    // }
}