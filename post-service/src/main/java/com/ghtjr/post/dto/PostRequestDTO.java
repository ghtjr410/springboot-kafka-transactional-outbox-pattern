package com.ghtjr.post.dto;

public record PostRequestDTO(
        String userUuid,
        String nickname,
        String title,
        String content
) {
}
