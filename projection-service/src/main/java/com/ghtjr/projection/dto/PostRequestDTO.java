package com.ghtjr.projection.dto;

public record PostRequestDTO(
        String userUuid,
        String nickname,
        String title,
        String content
) {
}
