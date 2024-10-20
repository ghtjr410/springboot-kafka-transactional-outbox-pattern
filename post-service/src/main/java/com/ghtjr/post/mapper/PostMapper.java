package com.ghtjr.post.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.avro.PostCreatedEvent;
import com.ghtjr.post.dto.PostRequestDTO;
import com.ghtjr.post.dto.PostResponseDTO;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.util.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class PostMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Post toEntity(PostRequestDTO postRequestDTO) {
        return Post.builder()
                .uuid(UUID.randomUUID().toString())
                .userUuid(postRequestDTO.userUuid())
                .nickname(postRequestDTO.nickname())
                .title(postRequestDTO.title())
                .content(postRequestDTO.content())
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();
    }

    public PostResponseDTO toResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getUuid(),
                post.getUserUuid(),
                post.getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedDate(),
                post.getUpdatedDate()
        );
    }

    public OutboxEvent toOutboxEvent(Post post) throws JsonProcessingException {
        try {
            // Avro로 생성된 PostCreatedEvent 객체 생성
            PostCreatedEvent event = new PostCreatedEvent();
            event.setPostId(post.getUuid());
            event.setUserUuid(post.getUserUuid());
            event.setNickname(post.getNickname());
            event.setTitle(post.getTitle());
            event.setContent(post.getContent());
            event.setCreatedDate(post.getCreatedDate().getTime());
            event.setUpdatedDate(post.getUpdatedDate().getTime());

            // Avro 객체를 JSON 문자열로 직렬화
            String payload = avroToJson(event);

            return OutboxEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .postId(post.getUuid())
                    .eventType("PostCreatedEvent")
                    .payload(payload)
                    .createdAt(new Date())
                    .sagaStatus(SagaStatus.CREATED)
                    .processed(false)
                    .build();
        } catch (IOException e) {
            // 예외 로깅
            log.error("Error converting Post to OutboxEvent: {}", e.getMessage(), e);
            // 예외를 다시 던져 상위에서 처리
            throw new RuntimeException("Failed to create OutboxEvent", e);
        }
    }

    // Avro 객체를 JSON 문자열로 변환하는 메서드
    private String avroToJson(PostCreatedEvent event) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DatumWriter<PostCreatedEvent> writer = new SpecificDatumWriter<>(PostCreatedEvent.class);
        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(PostCreatedEvent.getClassSchema(), outputStream);
        writer.write(event, jsonEncoder);
        jsonEncoder.flush();
        outputStream.flush();
        return outputStream.toString();
    }
}
