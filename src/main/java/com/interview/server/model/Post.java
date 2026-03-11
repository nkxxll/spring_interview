package com.interview.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Long version = 1L;

    @Column(columnDefinition = "TEXT") // Allows for long content/articles
    private String content;

    // One Post can have many Comments
    // mappedBy refers to the "post" field in the Comment class
    @OneToMany(
        mappedBy = "post",
        cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    private List<Comment> comments = new ArrayList<>();

    // Self-referencing Many-to-Many for "Related Posts"
    @ManyToMany
    @JoinTable(
        name = "post_links",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "related_post_id")
    )
    @JsonIgnoreProperties({ "relatedPosts", "comments" })
    private Set<Post> relatedPosts = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Set<Post> getRelatedPosts() {
        return relatedPosts;
    }

    public void setRelatedPosts(Set<Post> relatedPosts) {
        this.relatedPosts = relatedPosts;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
