package com.ghtjr.post.repository;

import com.ghtjr.post.model.OutboxEvent;
import com.ghtjr.post.util.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    // un-processed records
    List<OutboxEvent> findByProcessedFalseAndSagaStatus(SagaStatus sagaStatus);

    boolean existsByEventId(String eventId);
    OutboxEvent findByEventId(String eventId);
//    Post findByUuid(String uuid);
}
