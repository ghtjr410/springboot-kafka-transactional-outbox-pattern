package com.ghtjr.projection.config;

import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.projection.service.PostCompensationEventService;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${projection.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    private final PostCompensationEventService postCompensationEventService;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka Consumer 설정
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // 멱등성을 위한 설정
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 오프셋 설정: earliest로 설정하여 처음부터 메시지를 소비
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl); // Schema Registry URL

        // Specific Avro Reader 사용
        configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return configProps;
    }

    @Bean
    public ConsumerFactory<String, PostCreatedEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        // 재시도 간격과 횟수 설정 (1초 간격으로 최대 3번 재시도)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(1.0); // 지수 증가 없이 고정 간격 사용
        backOff.setMaxInterval(1000L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(        // 회복자 설정
                (consumerRecord, exception) -> {
                    String eventId = (String) consumerRecord.key();
                    PostCreatedEvent postCreatedEvent = (PostCreatedEvent) consumerRecord.value();
                    log.error("최종 재시도 실패. 보상 트랜잭션을 수행합니다. eventId={}, error={}", eventId, exception.getMessage());
                    postCompensationEventService.processEvent(eventId, postCreatedEvent);
                },
                backOff);

        // 재시도 시도 시 로그를 남기기 위한 리스너 추가
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) -> {
            log.warn("재시도 시도 중입니다. 시도 횟수: {}, 예외: {}", deliveryAttempt, exception.getMessage());
        });
        // 필요에 따라 특정 예외를 재시도하거나 무시하도록 설정할 수 있습니다.
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PostCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PostCreatedEvent> consumerFactory,
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, PostCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        // 수동 커밋 설정 (필요에 따라)
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL);
        // 사용자 정의 에러 핸들러 설정
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}