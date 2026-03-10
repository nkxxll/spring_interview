package com.interview.server.repository;

import com.interview.server.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

// love that haha
public interface PostRepository extends JpaRepository<Post, Long> {}
