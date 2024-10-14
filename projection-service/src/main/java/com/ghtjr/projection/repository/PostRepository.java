package com.ghtjr.projection.repository;

import com.ghtjr.projection.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Post findByUuid(String uuid);
}
