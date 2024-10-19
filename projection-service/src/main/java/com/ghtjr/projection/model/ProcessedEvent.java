package com.ghtjr.projection.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "processed_events")
public class ProcessedEvent {
    @Id
    private String Id;

    @Indexed(unique = true)
    private String eventId;

    private String status; // PROCESSING, COMPLETED, FAILED
}
