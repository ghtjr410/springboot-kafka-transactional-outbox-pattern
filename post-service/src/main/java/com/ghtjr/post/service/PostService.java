package com.ghtjr.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ghtjr.post.dto.PostRequestDTO;
import com.ghtjr.post.dto.PostResponseDTO;
import com.ghtjr.post.mapper.PostMapper;
import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.repository.OutboxEventRepository;
import com.ghtjr.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) throws JsonProcessingException {

        Post post = postMapper.toEntity(postRequestDTO);
        post = postRepository.save(post);

        OutboxEvent outboxEvent = postMapper.toOutboxEvent(post);
        outboxEventRepository.save(outboxEvent);

        return postMapper.toResponseDTO(post);
    }
}
