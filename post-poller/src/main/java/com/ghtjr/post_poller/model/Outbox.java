package com.ghtjr.post_poller.model;

import com.ghtjr.post_poller.util.SagaStatus;
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
@Table(name = "OUTBOX")
@Builder
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;

    private String payload;

    private Date createdAt;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    private Boolean processed;
}
