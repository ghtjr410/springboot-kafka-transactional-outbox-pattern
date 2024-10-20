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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) {

        try {
            Post post = postMapper.toEntity(postRequestDTO);
            post = postRepository.save(post);

            OutboxEvent outboxEvent = postMapper.toOutboxEvent(post);
            outboxEventRepository.save(outboxEvent);

            System.out.println("데이터 삽입 성공?");
            return postMapper.toResponseDTO(post);
        } catch (Exception e) {
            // 예외 로그 출력
            log.error("Error occurred while creating post: ", e);
            // 예외를 다시 던져 트랜잭션이 롤백되도록 함
            throw new RuntimeException("Error occurred while creating post", e);
        }
    }
}
