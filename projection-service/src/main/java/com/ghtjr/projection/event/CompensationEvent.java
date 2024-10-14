package com.ghtjr.projection.event;

import lombok.Data;

@Data
public class CompensationEvent {
    private String postId;
    private String reason;
}
