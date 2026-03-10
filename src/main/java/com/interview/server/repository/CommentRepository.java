package com.interview.server.repository;

import com.interview.server.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    public List<Comment> findByTextContainingOrAuthorContaining(String author, String text);
}
