package com.ghtjr.post.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtjr.post.model.Outbox;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.util.SagaStatus;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PostEntityToOutboxEntityMapper {

    @SneakyThrows
    public Outbox map (Post post) {
        return
                Outbox.builder()
                        .aggregateId(post.getUuid())
                        .payload(new ObjectMapper().writeValueAsString(post))
                        .createdAt(new Date())
                        .sagaStatus(SagaStatus.CREATED)
                        .processed(false)
                        .build();
    }
}
