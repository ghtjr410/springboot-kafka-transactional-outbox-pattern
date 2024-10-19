package com.ghtjr.post.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.dto.PostRequestDTO;
import com.ghtjr.post.dto.PostResponseDTO;
import com.ghtjr.post.event.PostCreatedEvent;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.util.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
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
        PostCreatedEvent event = new PostCreatedEvent();
        event.setPostId(post.getUuid());
        event.setUserUuid(post.getUserUuid());
        event.setNickname(post.getNickname());
        event.setTitle(post.getTitle());
        event.setContent(post.getContent());
        event.setCreatedDate(post.getCreatedDate().getTime());
        event.setUpdatedDate(post.getUpdatedDate().getTime());

        return OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .postId(post.getUuid())
                .eventType("PostCreatedEvent")
                .payload(objectMapper.writeValueAsString(event))
                .createdAt(new Date())
                .sagaStatus(SagaStatus.CREATED)
                .processed(false)
                .build();
    }
}
