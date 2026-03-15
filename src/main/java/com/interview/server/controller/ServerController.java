package com.interview.server.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.interview.server.model.AocResult;
import com.interview.server.model.AuditEvent;
import com.interview.server.model.Comment;
import com.interview.server.model.Diff;
import com.interview.server.model.Post;
import com.interview.server.model.PostView;
import com.interview.server.repository.CommentRepository;
import com.interview.server.repository.PostRepository;
import com.interview.server.repository.DiffRepository;
import com.interview.server.service.AocService;
import com.interview.server.service.AuditService;
import com.interview.server.service.ViewTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class ServerController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DiffRepository diffRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AocService aocService;

    @Autowired
    private ViewTrackingService viewTrackingService;

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

    @GetMapping("/post/search")
    public List<Post> searchPost(@RequestParam(required = true) String q) {
        List<Post> posts =
            postRepository.findByTitleContainingOrContentContaining(q, q);
        return posts;
    }

    @GetMapping("/comment/search")
    public List<Comment> searchComment(
        @RequestParam(required = true) String q
    ) {
        List<Comment> comments =
            commentRepository.findByTextContainingOrAuthorContaining(q, q);
        return comments;
    }

    @GetMapping("/comment/show")
    public List<Post> showComments() {
        List<Post> posts = postRepository.findAll();
        return posts;
    }

    @GetMapping("/post/{id}/diff")
    public List<Diff> getPostDiffs(@PathVariable Long id) {
        return diffRepository.findByPostId(id);
    }

    @PostMapping("/post/{id}/update")
    public Post updatePost(
        @PathVariable Long id,
        @RequestBody Post post,
        HttpServletRequest request
    ) {
        if (
            post.getRelatedPosts() != null && !post.getRelatedPosts().isEmpty()
        ) {
            Set<Post> managed = post
                .getRelatedPosts()
                .stream()
                .filter(rp -> rp.getId() != null)
                .map(rp -> postRepository.findById(rp.getId()).orElseThrow())
                .collect(Collectors.toSet());
            post.setRelatedPosts(managed);
        }
        post.setId(id);
        Post oldPost = postRepository.findById(id).orElseThrow();
        Diff diff = Diff.fromPosts(post, oldPost);
        diffRepository.save(diff);
        Post saved = postRepository.save(post);
        auditService.logPostUpdated(saved, request);
        return saved;
    }

    @PostMapping("/post/create")
    @WithSpan
    public Post createPost(@RequestBody Post post, HttpServletRequest request) {
        if (
            post.getRelatedPosts() != null && !post.getRelatedPosts().isEmpty()
        ) {
            Set<Post> managed = post
                .getRelatedPosts()
                .stream()
                .filter(rp -> rp.getId() != null)
                .map(rp -> postRepository.findById(rp.getId()).orElseThrow())
                .collect(Collectors.toSet());
            post.setRelatedPosts(managed);
        }
        Post saved = postRepository.save(post);
        auditService.logPostCreated(saved, request);
        return saved;
    }

    @PostMapping("/comment/create")
    public Comment createComment(
        @RequestBody Comment comment,
        HttpServletRequest request
    ) {
        Comment savedComment = commentRepository.save(comment);
        auditService.logCommentAdded(savedComment, request);
        return savedComment;
    }

    @GetMapping("/post/{id}/audit")
    public List<AuditEvent> getPostAudit(@PathVariable Long id) {
        UUID postUuid = new UUID(0, id);
        return auditService.getEventsForPost(postUuid);
    }

    @PostMapping("/post/{id}/view")
    public ResponseEntity<Void> recordView(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        viewTrackingService.recordView(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{id}/view-count")
    public ResponseEntity<Long> getViewCount(@PathVariable Long id) {
        return ResponseEntity.ok(viewTrackingService.getViewCount(id));
    }

    @PostMapping("/aoc/solve")
    public AocResult solveAoc(@RequestBody AocRequest request) {
        return aocService.solve(
            request.day(),
            request.task(),
            request.input(),
            request.expectedOutput(),
            request.test()
        );
    }

    @GetMapping("/aoc/history")
    public List<AocResult> aocHistory() {
        return aocService.getHistory();
    }

    record AocRequest(int day, int task, String input, String expectedOutput, boolean test) {}

    @GetMapping("/post/{id}/views")
    public List<PostView> getViews(
        @PathVariable Long id,
        @RequestParam String date
    ) {
        return viewTrackingService.getViewsForDate(id, LocalDate.parse(date));
    }
}
