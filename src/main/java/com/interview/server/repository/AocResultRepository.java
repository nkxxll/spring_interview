package com.interview.server.repository;

import com.interview.server.model.AocResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface AocResultRepository
    extends CassandraRepository<AocResult, UUID> {

    List<AocResult> findByDay(int day);

    @org.springframework.data.cassandra.repository.Query(
        "SELECT * FROM aoc_results PER PARTITION LIMIT 10"
    )
    List<AocResult> findRecent();
}
