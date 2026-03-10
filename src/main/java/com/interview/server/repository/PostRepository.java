package com.interview.server.repository;

import com.interview.server.model.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// love that haha
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTitleContainingOrContentContaining(String title, String content);
}
