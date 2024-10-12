package com.ghtjr.post.mapper;

import com.ghtjr.post.dto.PostRequestDTO;
import com.ghtjr.post.model.Post;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PostDTOtoEntityMapper {

    public Post map(PostRequestDTO postRequestDTO) {
        return
                Post.builder()
                        .userUuid(postRequestDTO.userUuid())
                        .nickname(postRequestDTO.nickname())
                        .title(postRequestDTO.title())
                        .content(postRequestDTO.content())
                        .createdDate(new Date())
                        .updatedDate(new Date())
                        .build();

    }
}
