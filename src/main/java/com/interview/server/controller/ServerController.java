package com.interview.server.controller;

import com.interview.server.model.Comment;
import com.interview.server.model.Post;
import com.interview.server.repository.CommentRepository;
import com.interview.server.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class ServerController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/newheader")
    public String newHeader() {
        return "New Header From the Server";
    }

    @GetMapping("/post/show")
    public List<Post> showPosts() {
        List<Post> posts = postRepository.findAll();
        return posts;
    }

    @GetMapping("/comment/show")
    public List<Post> showComments() {
        List<Post> posts = postRepository.findAll();
        return posts;
    }

    @PostMapping("/post/create")
    public Post createPost(@RequestBody Post post, HttpServletRequest request) {
        if (post.getRelatedPosts() != null && !post.getRelatedPosts().isEmpty()) {
            Set<Post> managed = post.getRelatedPosts().stream()
                .filter(rp -> rp.getId() != null)
                .map(rp -> postRepository.findById(rp.getId()).orElseThrow())
                .collect(Collectors.toSet());
            post.setRelatedPosts(managed);
        }
        return postRepository.save(post);
    }

    @PostMapping("/comment/create")
    public Comment createComment(
        @RequestBody Comment comment,
        HttpServletRequest request
    ) {
        Comment savedComment = commentRepository.save(comment);
        return savedComment;
    }
}
