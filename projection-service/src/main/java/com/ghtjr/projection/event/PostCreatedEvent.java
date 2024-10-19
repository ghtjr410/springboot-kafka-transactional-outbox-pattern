package com.ghtjr.projection.event;

import lombok.Data;

@Data
public class PostCreatedEvent {
    private String eventId;
    private String postId;
    private String userUuid;
    private String nickname;
    private String title;
    private String content;
    private Long createdDate;
    private Long updatedDate;
}
