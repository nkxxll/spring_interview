package com.interview.server.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class PostViewKey implements Serializable {

    @PrimaryKeyColumn(
        name = "post_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID postId;

    @PrimaryKeyColumn(
        name = "view_date",
        ordinal = 1,
        type = PrimaryKeyType.PARTITIONED
    )
    private LocalDate viewDate;

    @PrimaryKeyColumn(
        name = "view_time",
        ordinal = 2,
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.DESCENDING
    )
    private UUID viewTime;

    public PostViewKey() {}

    public PostViewKey(UUID postId, LocalDate viewDate, UUID viewTime) {
        this.postId = postId;
        this.viewDate = viewDate;
        this.viewTime = viewTime;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public LocalDate getViewDate() {
        return viewDate;
    }

    public void setViewDate(LocalDate viewDate) {
        this.viewDate = viewDate;
    }

    public UUID getViewTime() {
        return viewTime;
    }

    public void setViewTime(UUID viewTime) {
        this.viewTime = viewTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostViewKey other)) return false;
        return Objects.equals(postId, other.postId) &&
            Objects.equals(viewDate, other.viewDate) &&
            Objects.equals(viewTime, other.viewTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, viewDate, viewTime);
    }
}
