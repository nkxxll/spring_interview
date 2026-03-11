package com.interview.server.model;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("post_view_counts")
public class PostViewCount {

    @PrimaryKeyColumn(
        name = "post_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID postId;

    @Column("total_views")
    private long totalViews;

    public PostViewCount() {}

    public PostViewCount(UUID postId, long totalViews) {
        this.postId = postId;
        this.totalViews = totalViews;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(long totalViews) {
        this.totalViews = totalViews;
    }
}
