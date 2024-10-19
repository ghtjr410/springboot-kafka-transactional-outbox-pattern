package com.ghtjr.projection.dto;

import java.util.Date;

public record PostResponseDTO(
        String uuid,
        String userUuid,
        String nickname,
        String title,
        String content,
        Date createdDate,
        Date updatedDate
) {
}
