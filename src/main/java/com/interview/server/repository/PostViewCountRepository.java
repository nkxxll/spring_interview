package com.interview.server.repository;

import com.interview.server.model.PostViewCount;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PostViewCountRepository
    extends CassandraRepository<PostViewCount, UUID> {}
