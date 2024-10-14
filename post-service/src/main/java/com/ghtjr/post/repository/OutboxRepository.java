package com.ghtjr.post.repository;

import com.ghtjr.post.model.Outbox;
import com.ghtjr.post.model.Post;
import com.ghtjr.post.util.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    // un-processed records
    List<Outbox> findByProcessedFalseAndSagaStatus(SagaStatus sagaStatus);

    Outbox findByAggregateId(String uuid);
//    Post findByUuid(String uuid);
}
