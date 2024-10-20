package com.ghtjr.post.config;

import com.ghtjr.projection.avro.PostCompensationEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
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
public class KafkaConsumerConfig {

     @Value("${spring.kafka.bootstrap-servers}")
     private String bootstrapServers;

     @Value("${spring.kafka.consumer.group-id}")
     private String groupId;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

     // Kafka Consumer 공통 설정
     @Bean
     public Map<String, Object> consumerConfigs() {
         Map<String, Object> configProps = new HashMap<>();

         // Kafka Consumer 설정
         configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

         // 멱등성을 위한 자동 커밋 비활성화
         configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

         // 오프셋 설정: earliest로 설정하여 처음부터 메시지를 소비
         configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

         // 메시지 직렬화/역직렬화 설정
         configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
         configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

         // Specific Avro Reader 사용
         configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

         return configProps;
     }
     // ConsumerFactory 생성
     @Bean
     public ConsumerFactory<String, PostCompensationEvent> consumerFactory() {
         return new DefaultKafkaConsumerFactory<>(consumerConfigs());
     }
     // 에러 핸들러 설정 (재시도 로직 포함)
     @Bean
     public DefaultErrorHandler errorHandler() {
         // 1초 간격으로 최대 3번 재시도 설정
         ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
         backOff.setInitialInterval(1000L); // 1초 간격으로 재시도
         backOff.setMultiplier(1.0);        // 고정 간격 사용
         backOff.setMaxInterval(1000L);     // 최대 간격 설정

         // 특정 예외를 무시하거나 재시도할 수 있도록 추가 설정 가능
         return new DefaultErrorHandler(backOff);
     }

     @Bean
     public ConcurrentKafkaListenerContainerFactory<String, PostCompensationEvent> kafkaListenerContainerFactory(
             ConsumerFactory<String, PostCompensationEvent> consumerFactory,
             DefaultErrorHandler errorHandler) {

         ConcurrentKafkaListenerContainerFactory<String, PostCompensationEvent> factory =
                 new ConcurrentKafkaListenerContainerFactory<>();

         factory.setConsumerFactory(consumerFactory);

         // 수동 커밋 설정 (AckMode.MANUAL)
         factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

         // 에러 핸들러 설정
         factory.setCommonErrorHandler(errorHandler);

         return factory;
     }
}
