package com.interview.server.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.interview.server.model.AuditEvent;
import com.interview.server.model.Comment;
import com.interview.server.model.Post;
import com.interview.server.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditEventRepository auditEventRepository;

    public void logPostCreated(Post post, HttpServletRequest request) {
        AuditEvent event = buildEvent(
            post.getId(),
            "POST_CREATED",
            request,
            String.format(
                "{\"title\":\"%s\"}",
                escapeJson(post.getTitle())
            )
        );
        auditEventRepository.save(event);
    }

    public void logPostUpdated(Post post, HttpServletRequest request) {
        AuditEvent event = buildEvent(
            post.getId(),
            "POST_UPDATED",
            request,
            String.format(
                "{\"title\":\"%s\"}",
                escapeJson(post.getTitle())
            )
        );
        auditEventRepository.save(event);
    }

    public List<AuditEvent> getEventsForPost(UUID postId) {
        return auditEventRepository.findByPostId(postId);
    }

    public void logCommentAdded(Comment comment, HttpServletRequest request) {
        Post post = comment.getPost();
        if (post == null) {
            return;
        }
        AuditEvent event = buildEvent(
            post.getId(),
            "COMMENT_ADDED",
            request,
            String.format(
                "{\"commentId\":%d,\"author\":\"%s\"}",
                comment.getId(),
                escapeJson(comment.getAuthor())
            )
        );
        auditEventRepository.save(event);
    }

    private AuditEvent buildEvent(
        Long postId,
        String eventType,
        HttpServletRequest request,
        String detail
    ) {
        UUID timeUuid = Uuids.timeBased();
        return new AuditEvent(
            uuidFromLong(postId),
            timeUuid,
            UUID.randomUUID(),
            eventType,
            request.getRemoteAddr(),
            detail
        );
    }

    private UUID uuidFromLong(Long id) {
        return new UUID(0, id);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
