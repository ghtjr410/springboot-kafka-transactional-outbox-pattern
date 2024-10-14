package com.ghtjr.projection.repository;

import com.ghtjr.projection.model.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {
    boolean existsByEventId(String eventId);
}
