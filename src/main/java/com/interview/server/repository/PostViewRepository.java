package com.interview.server.repository;

import com.interview.server.model.PostView;
import com.interview.server.model.PostViewKey;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

public interface PostViewRepository
    extends CassandraRepository<PostView, PostViewKey> {

    @Query("SELECT * FROM post_views WHERE post_id = ?0 AND view_date = ?1")
    List<PostView> findByPostIdAndViewDate(UUID postId, LocalDate viewDate);
}
