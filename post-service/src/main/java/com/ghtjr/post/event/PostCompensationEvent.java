package com.ghtjr.post.event;

import lombok.Data;

@Data
public class PostCompensationEvent {
    private String eventId; // 고유한 이벤트 Id
    private String postId; // 고유한 게시글 ID
    private String reason; // 보상 이유
}
