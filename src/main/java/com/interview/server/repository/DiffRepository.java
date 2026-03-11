package com.interview.server.repository;

import com.interview.server.model.Diff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiffRepository extends JpaRepository<Diff, Long> {
    List<Diff> findByPostId(Long postId);
}
