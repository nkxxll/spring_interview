package com.interview.server.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("post_views")
public class PostView {

    @PrimaryKey
    private PostViewKey key;

    @Column("viewer_ip")
    private String viewerIp;

    @Column("user_agent")
    private String userAgent;

    public PostView() {}

    public PostView(PostViewKey key, String viewerIp, String userAgent) {
        this.key = key;
        this.viewerIp = viewerIp;
        this.userAgent = userAgent;
    }

    public PostViewKey getKey() {
        return key;
    }

    public void setKey(PostViewKey key) {
        this.key = key;
    }

    public String getViewerIp() {
        return viewerIp;
    }

    public void setViewerIp(String viewerIp) {
        this.viewerIp = viewerIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
