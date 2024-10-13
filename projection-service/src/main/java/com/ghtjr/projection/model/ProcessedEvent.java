package com.ghtjr.projection.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    private String eventId;
}
