package com.ghtjr.post.model;

import com.ghtjr.post.util.SagaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OUTBOX_EVENT")
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String postId;

    private String eventType;

    private String payload;

    private Date createdAt;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    private Boolean processed;
}
