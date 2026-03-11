package com.interview.server.repository;

import com.interview.server.model.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface AuditEventRepository
    extends CassandraRepository<AuditEvent, UUID> {

    List<AuditEvent> findByPostId(UUID postId);
}
