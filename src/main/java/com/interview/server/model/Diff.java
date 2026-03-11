package com.interview.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "diffs")
public class Diff {

    protected Diff() {}

    private Diff(Builder builder) {
        this.titleDiff = builder.titleDiff;
        this.contentDiff = builder.contentDiff;
        this.relatedPostsChanges = builder.relatedPostsChanges;
        this.commentsChanges = builder.commentsChanges;
        this.commentSnapshot = builder.commentSnapshot;
        this.postsSnapshot = builder.postsSnapshot;
        this.parent = builder.parent;
        this.post = builder.post;
    }

    public static Builder builder(String titleDiff, String contentDiff, Post post) {
        return new Builder(titleDiff, contentDiff, post);
    }

    public static class Builder {

        private String titleDiff;
        private String contentDiff;
        private String relatedPostsChanges;
        private String commentsChanges;
        private Post post;

        private List<Comment> commentSnapshot;
        private Set<Post> postsSnapshot;
        private Diff parent;

        public Builder(String titleDiff, String contentDiff, Post post) {
            this.titleDiff = titleDiff;
            this.contentDiff = contentDiff;
            this.post = post;
        }

        public Builder relatedPostsChanges(String relatedPostsChanges) {
            this.relatedPostsChanges = relatedPostsChanges;
            return this;
        }

        public Builder commentsChanges(String commentsChanges) {
            this.commentsChanges = commentsChanges;
            return this;
        }

        public Builder comments(List<Comment> comments) {
            this.commentSnapshot = comments;
            return this;
        }

        public Builder posts(Set<Post> posts) {
            this.postsSnapshot = posts;
            return this;
        }

        public Builder parent(Diff parent) {
            this.parent = parent;
            return this;
        }

        public Diff build() {
            return new Diff(this);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titleDiff;

    @Column(columnDefinition = "TEXT")
    private String contentDiff;

    @Column(columnDefinition = "TEXT")
    private String relatedPostsChanges;

    @Column(columnDefinition = "TEXT")
    private String commentsChanges;

    @ManyToMany
    @JoinTable(
        name = "diff_comments",
        joinColumns = @JoinColumn(name = "diff_id"),
        inverseJoinColumns = @JoinColumn(name = "comment_id")
    )
    private List<Comment> commentSnapshot = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "diff_posts",
        joinColumns = @JoinColumn(name = "diff_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> postsSnapshot = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Diff parent;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties({"comments", "relatedPosts"})
    private Post post;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Optional<String> getTitleDiff() {
        return Optional.ofNullable(titleDiff);
    }

    public void setTitleDiff(String titleDiff) {
        this.titleDiff = titleDiff;
    }

    public Optional<String> getContentDiff() {
        return Optional.ofNullable(contentDiff);
    }

    public void setContentDiff(String contentDiff) {
        this.contentDiff = contentDiff;
    }

    public Optional<String> getRelatedPostsChanges() {
        return Optional.ofNullable(relatedPostsChanges);
    }

    public void setRelatedPostsChanges(String relatedPostsChanges) {
        this.relatedPostsChanges = relatedPostsChanges;
    }

    public Optional<String> getCommentsChanges() {
        return Optional.ofNullable(commentsChanges);
    }

    public void setCommentsChanges(String commentsChanges) {
        this.commentsChanges = commentsChanges;
    }

    public List<Comment> getCommentSnapshot() {
        return commentSnapshot;
    }

    public void setCommentSnapshot(List<Comment> commentSnapshot) {
        this.commentSnapshot = commentSnapshot;
    }

    public Set<Post> getPostsSnapshot() {
        return postsSnapshot;
    }

    public void setPostsSnapshot(Set<Post> postsSnapshot) {
        this.postsSnapshot = postsSnapshot;
    }

    public Optional<Diff> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(Diff parent) {
        this.parent = parent;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public static Diff fromPosts(Post newPost, Post oldPost) {
        String titleDiff = null;
        String contentDiff = null;
        List<Comment> commentSnapshot;
        Set<Post> postSnapshot;

        String newTitle = newPost.getTitle();
        String oldTitle = oldPost.getTitle();
        if (!newTitle.equals(oldTitle)) {
            titleDiff = oldTitle;
        }
        List<String> newContent = Arrays.asList(
            newPost.getContent().split("\n")
        );
        List<String> oldContent = Arrays.asList(
            oldPost.getContent().split("\n")
        );
        String newPostName = String.format(
            "Post-i%d-r%d",
            oldPost.getId(),
            oldPost.getVersion() + 1
        );
        String oldPostName = String.format(
            "Post-i%d-r%d",
            oldPost.getId(),
            oldPost.getVersion()
        );

        Patch<String> patch = DiffUtils.diff(oldContent, newContent);

        if (!patch.getDeltas().isEmpty()) {
            //generating unified diff format
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                oldPostName,
                newPostName,
                oldContent,
                patch,
                0
            );
            contentDiff = String.join("\n", unifiedDiff);
        }
        // Compute related posts changes
        String relatedPostsChanges = null;
        Set<Long> oldPostIds = oldPost.getRelatedPosts().stream()
            .map(Post::getId).collect(Collectors.toSet());
        Set<Long> newPostIds = newPost.getRelatedPosts().stream()
            .map(Post::getId).collect(Collectors.toSet());
        if (!oldPostIds.equals(newPostIds)) {
            List<String> lines = new ArrayList<>();
            // Find added posts
            for (Post p : newPost.getRelatedPosts()) {
                if (!oldPostIds.contains(p.getId())) {
                    lines.add("+ " + (p.getTitle() != null ? p.getTitle() : "Post #" + p.getId()));
                }
            }
            // Find removed posts
            for (Post p : oldPost.getRelatedPosts()) {
                if (!newPostIds.contains(p.getId())) {
                    lines.add("- " + (p.getTitle() != null ? p.getTitle() : "Post #" + p.getId()));
                }
            }
            if (!lines.isEmpty()) {
                relatedPostsChanges = String.join("\n", lines);
            }
        }

        // Compute comments changes (only if new post actually carries comments)
        String commentsChanges = null;
        Set<Long> oldCommentIds = oldPost.getComments().stream()
            .map(Comment::getId).collect(Collectors.toSet());
        Set<Long> newCommentIds = newPost.getComments().stream()
            .map(Comment::getId).collect(Collectors.toSet());
        boolean commentsChanged = !newPost.getComments().isEmpty()
            && !oldCommentIds.equals(newCommentIds);
        if (commentsChanged) {
            List<String> lines = new ArrayList<>();
            for (Comment c : newPost.getComments()) {
                if (!oldCommentIds.contains(c.getId())) {
                    String label = c.getText() != null ? c.getText() : "Comment #" + c.getId();
                    lines.add("+ " + label);
                }
            }
            for (Comment c : oldPost.getComments()) {
                if (!newCommentIds.contains(c.getId())) {
                    String label = c.getText() != null ? c.getText() : "Comment #" + c.getId();
                    lines.add("- " + label);
                }
            }
            if (!lines.isEmpty()) {
                commentsChanges = String.join("\n", lines);
            }
        }

        Diff diff = Diff.builder(titleDiff, contentDiff, oldPost)
            .relatedPostsChanges(relatedPostsChanges)
            .commentsChanges(commentsChanges)
            .build();

        // if comments
        if (commentsChanged) {
            diff.setCommentSnapshot(new ArrayList<>(oldPost.getComments()));
        }
        // if posts
        if (!oldPostIds.equals(newPostIds)) {
            diff.setPostsSnapshot(new HashSet<>(oldPost.getRelatedPosts()));
        }

        return diff;
    }
}
