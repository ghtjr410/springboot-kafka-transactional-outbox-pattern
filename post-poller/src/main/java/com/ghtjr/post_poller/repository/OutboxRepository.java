package com.ghtjr.post_poller.repository;

import com.ghtjr.post_poller.model.Outbox;
import com.ghtjr.post_poller.util.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    // un-processed records
    List<Outbox> findByProcessedFalseAndSagaStatus(SagaStatus sagaStatus);
}
