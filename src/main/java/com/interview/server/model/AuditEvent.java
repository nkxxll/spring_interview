package com.interview.server.model;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("audit_events")
public class AuditEvent {

    @PrimaryKeyColumn(
        name = "post_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID postId;

    @PrimaryKeyColumn(
        name = "event_time",
        ordinal = 1,
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.DESCENDING
    )
    private UUID eventTime;

    @Column("event_id")
    private UUID eventId;

    @Column("event_type")
    private String eventType;

    @Column("actor_ip")
    private String actorIp;

    @Column("detail")
    private String detail;

    public AuditEvent() {}

    public AuditEvent(
        UUID postId,
        UUID eventTime,
        UUID eventId,
        String eventType,
        String actorIp,
        String detail
    ) {
        this.postId = postId;
        this.eventTime = eventTime;
        this.eventId = eventId;
        this.eventType = eventType;
        this.actorIp = actorIp;
        this.detail = detail;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getEventTime() {
        return eventTime;
    }

    public void setEventTime(UUID eventTime) {
        this.eventTime = eventTime;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActorIp() {
        return actorIp;
    }

    public void setActorIp(String actorIp) {
        this.actorIp = actorIp;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
