package com.ghtjr.post.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class PostCompensationEvent {
    private String postId;
    private String reason;
}
