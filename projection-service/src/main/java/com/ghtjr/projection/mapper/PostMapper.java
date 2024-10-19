package com.ghtjr.projection.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.projection.dto.PostResponseDTO;
import com.ghtjr.projection.event.PostCreatedEvent;
import com.ghtjr.projection.model.Post;
import com.ghtjr.projection.model.ProcessedEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PostMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Post toEntity(PostCreatedEvent event) {
        Post post = new Post();
        post.setUuid(event.getPostId());
        post.setUserUuid(event.getUserUuid());
        post.setNickname(event.getNickname());
        post.setTitle(event.getTitle());
        post.setContent(event.getContent());
        post.setCreatedDate(event.getCreatedDate());
        post.setUpdatedDate(event.getUpdatedDate());
        return post;
    }

    public PostResponseDTO toResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getUuid(),
                post.getUserUuid(),
                post.getNickname(),
                post.getTitle(),
                post.getContent(),
                new Date(post.getCreatedDate()),
                new Date(post.getUpdatedDate())
        );
    }

    public ProcessedEvent toProcessedEvent(String eventId) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setStatus("PROCESSING");
        return processedEvent;
    }

    public ProcessedEvent completeProcessedEvent(ProcessedEvent processedEvent) {
        processedEvent.setStatus("COMPLETED");
        return processedEvent;
    }

    public ProcessedEvent failProcessedEvent(ProcessedEvent processedEvent) {
        processedEvent.setStatus("FAILED");
        return processedEvent;
    }
}
