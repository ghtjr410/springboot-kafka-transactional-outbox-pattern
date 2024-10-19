package com.ghtjr.projection.event;

import lombok.Data;

@Data
public class CompensationEvent {
    private String eventId;
    private String postId;
    private String reason;
}
