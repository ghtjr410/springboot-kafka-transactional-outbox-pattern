package com.ghtjr.post.service;

import com.ghtjr.post.dto.PostRequestDTO;
import com.ghtjr.post.mapper.PostDTOtoEntityMapper;
import com.ghtjr.post.mapper.PostEntityToOutboxEntityMapper;
import com.ghtjr.post.model.Outbox;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.repository.OutboxRepository;
import com.ghtjr.post.repository.PostRepository;
import com.ghtjr.post.util.SagaStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostDTOtoEntityMapper postDTOtoEntityMapper;
    private final PostEntityToOutboxEntityMapper postEntityToOutboxEntityMapper;

    private final PostRepository postRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public Post createPost(PostRequestDTO postRequestDTO){

        Post post = postDTOtoEntityMapper.map(postRequestDTO);
        post = postRepository.save(post);

        Outbox outbox = postEntityToOutboxEntityMapper.map(post);
        outboxRepository.save(outbox);

        return post;
    }
}
