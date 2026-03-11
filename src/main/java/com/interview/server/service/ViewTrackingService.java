package com.interview.server.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.interview.server.model.PostView;
import com.interview.server.model.PostViewCount;
import com.interview.server.model.PostViewKey;
import com.interview.server.repository.PostViewCountRepository;
import com.interview.server.repository.PostViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Service;

@Service
public class ViewTrackingService {

    @Autowired
    private PostViewRepository postViewRepository;

    @Autowired
    private PostViewCountRepository postViewCountRepository;

    @Autowired
    private CassandraOperations cassandraOperations;

    public void recordView(Long postId, HttpServletRequest request) {
        UUID uuid = uuidFromLong(postId);
        LocalDate today = LocalDate.now();
        UUID viewTime = Uuids.timeBased();

        PostViewKey key = new PostViewKey(uuid, today, viewTime);
        PostView view = new PostView(
            key,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        postViewRepository.save(view);

        // Counter tables require UPDATE, not INSERT
        cassandraOperations.getCqlOperations().execute(
            "UPDATE post_view_counts SET total_views = total_views + 1 WHERE post_id = ?",
            uuid
        );
    }

    public long getViewCount(Long postId) {
        UUID uuid = uuidFromLong(postId);
        return postViewCountRepository.findById(uuid)
            .map(PostViewCount::getTotalViews)
            .orElse(0L);
    }

    public List<PostView> getViewsForDate(Long postId, LocalDate date) {
        UUID uuid = uuidFromLong(postId);
        return postViewRepository.findByPostIdAndViewDate(uuid, date);
    }

    private UUID uuidFromLong(Long id) {
        return new UUID(0, id);
    }
}
