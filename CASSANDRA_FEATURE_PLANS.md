# Cassandra Feature Plans

Three options for adding a Cassandra-backed feature alongside the existing H2/JPA setup.

---

## Option 1: Audit / Activity Log

### Goal
Record every API action (post created, post updated, comment added, etc.) as an immutable event log in Cassandra. Query the log by post or globally by time range.

### Why Cassandra?
- Append-only, never updated or deleted — perfect for Cassandra's LSM-tree storage
- Write-heavy (every API call = 1 write), read-infrequent (only when reviewing history)
- Queried by a known partition key (`post_id`) + time range — exactly how Cassandra clustering keys work

### Data Model

```
Table: audit_events
─────────────────────────────────────────────────
Partition Key:  post_id (UUID)
Clustering Key: event_time (TIMEUUID, DESC)
─────────────────────────────────────────────────
Columns:
  - event_id     : UUID
  - post_id      : UUID
  - event_type   : TEXT   ("POST_CREATED", "POST_UPDATED", "COMMENT_ADDED")
  - actor_ip     : TEXT   (from HttpServletRequest)
  - detail       : TEXT   (JSON summary of what changed)
  - event_time   : TIMEUUID
```

### Files to Create
| File | Purpose |
|------|---------|
| `model/AuditEvent.java` | Cassandra `@Table` entity with `@PrimaryKeyColumn` annotations |
| `repository/AuditEventRepository.java` | Extends `CassandraRepository<AuditEvent, UUID>` |
| `service/AuditService.java` | Thin service that builds and saves `AuditEvent` records |

### Files to Modify
| File | Change |
|------|--------|
| `ServerController.java` | Inject `AuditService`, call it in `createPost()`, `updatePost()`, `createComment()` |
| `ServerController.java` | Add `GET /api/post/{id}/audit` endpoint to query audit trail for a post |
| `application.properties` | Already has Cassandra config — just verify `keyspace-name` and `schema-action` |

### New API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/post/{id}/audit` | Return all audit events for a post, ordered by time DESC |
| `GET` | `/api/audit/recent?limit=50` | Return the N most recent global events (requires a second table or `ALLOW FILTERING`) |

### Implementation Steps
1. Create `AuditEvent` model with Spring Data Cassandra annotations (`@Table`, `@PrimaryKeyColumn`)
2. Create `AuditEventRepository` extending `CassandraRepository`
3. Create `AuditService` with methods like `logPostCreated(Post, request)`, `logPostUpdated(Post, request)`, `logCommentAdded(Comment, request)`
4. Inject `AuditService` into `ServerController` and call from existing endpoints
5. Add `GET /api/post/{id}/audit` endpoint
6. Test: create a post, update it, add a comment — then query the audit endpoint

### Complexity
**Low-Medium** — 3 new files, minor edits to 1 existing file. No changes to existing models or DB.

---

## Option 2: Post View Tracker

### Goal
Track every view of a post (who viewed it, when, from where). Show view counts and view history per post. Partition by post + date so data is naturally bucketed for time-range queries.

### Why Cassandra?
- Extremely write-heavy (every page load = 1 write), reads are aggregations
- Data is naturally time-bucketed (views per day)
- No relationships needed — each view is an independent event
- Demonstrates composite partition key (`post_id` + `view_date`)

### Data Model

```
Table: post_views
─────────────────────────────────────────────────
Partition Key:  (post_id: UUID, view_date: DATE)
Clustering Key: view_time (TIMEUUID, DESC)
─────────────────────────────────────────────────
Columns:
  - post_id    : UUID
  - view_date  : DATE   (date bucket, e.g. 2026-03-11)
  - view_time  : TIMEUUID
  - viewer_ip  : TEXT
  - user_agent : TEXT

Table: post_view_counts (counter table)
─────────────────────────────────────────────────
Partition Key:  post_id (UUID)
─────────────────────────────────────────────────
Columns:
  - post_id     : UUID
  - total_views : COUNTER
```

### Files to Create
| File | Purpose |
|------|---------|
| `model/PostView.java` | Cassandra entity for individual view events |
| `model/PostViewCount.java` | Cassandra counter table entity |
| `model/PostViewKey.java` | `@PrimaryKeyClass` for the composite key (`post_id` + `view_date` + `view_time`) |
| `repository/PostViewRepository.java` | Extends `CassandraRepository` |
| `repository/PostViewCountRepository.java` | Counter table repository |
| `service/ViewTrackingService.java` | Records views and increments counters |

### Files to Modify
| File | Change |
|------|--------|
| `ServerController.java` | Add `GET /api/post/{id}/views` and `GET /api/post/{id}/view-count` endpoints |
| `ServerController.java` | Call `viewTrackingService.recordView()` from `showPosts()` or add a dedicated view endpoint |

### New API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/post/{id}/view` | Record a view event (called by frontend) |
| `GET` | `/api/post/{id}/view-count` | Return total view count |
| `GET` | `/api/post/{id}/views?date=2026-03-11` | Return individual views for a specific date |

### Implementation Steps
1. Create `PostViewKey` with `@PrimaryKeyClass` — composite partition key + clustering key
2. Create `PostView` entity mapped to `post_views` table
3. Create `PostViewCount` entity for the counter table
4. Create repositories for both
5. Create `ViewTrackingService` that writes to both tables on each view
6. Add endpoints to `ServerController`
7. Update frontend `index.js` to call the view endpoint when a post is loaded
8. Test: load a post several times, check counts and view history

### Complexity
**Medium** — 6 new files, demonstrates composite keys and Cassandra counter tables. Counter tables have special rules (no non-counter columns mixed with counters) which is educational.

---

## Option 3: Posts-by-Tag Index

### Goal
Add tags to posts and maintain a Cassandra-backed `posts_by_tag` lookup table for fast tag-based queries. Tags are stored relationally in H2 (source of truth) and denormalized into Cassandra for fast reads — demonstrating the dual-database pattern.

### Why Cassandra?
- Demonstrates denormalization: same data in two stores for different access patterns
- Query pattern is fixed: "give me all posts with tag X, newest first"
- Write-on-change (only when tags are modified), read-heavy on the Cassandra side
- Shows how Cassandra tables are designed around queries, not entities

### Data Model

```
H2 (relational, source of truth):
  Table: tags        (id, name)
  Table: post_tags   (post_id, tag_id)  — join table

Cassandra (denormalized read model):
Table: posts_by_tag
─────────────────────────────────────────────────
Partition Key:  tag_name (TEXT)
Clustering Key: created_at (TIMESTAMP, DESC)
─────────────────────────────────────────────────
Columns:
  - tag_name   : TEXT
  - created_at : TIMESTAMP
  - post_id    : BIGINT  (matches H2 post ID)
  - post_title : TEXT    (denormalized copy)
  - snippet    : TEXT    (first 200 chars of content)
```

### Files to Create
| File | Purpose |
|------|---------|
| `model/Tag.java` | JPA entity for H2 (`@Entity`) |
| `model/PostsByTag.java` | Cassandra entity for the denormalized table |
| `model/PostsByTagKey.java` | `@PrimaryKeyClass` for composite key (`tag_name` + `created_at`) |
| `repository/TagRepository.java` | JPA repository for tags |
| `repository/PostsByTagRepository.java` | Cassandra repository |
| `service/TagService.java` | Manages tags in H2 and syncs to Cassandra |

### Files to Modify
| File | Change |
|------|--------|
| `model/Post.java` | Add `@ManyToMany` relationship to `Tag` |
| `ServerController.java` | Add tag management and tag-search endpoints |
| `application.properties` | No changes needed (Cassandra already configured) |

### New API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/post/{id}/tags` | Add tags to a post (writes to H2 + Cassandra) |
| `DELETE` | `/api/post/{id}/tags/{tag}` | Remove a tag |
| `GET` | `/api/tags/{tag}/posts` | Query posts by tag from Cassandra (fast) |
| `GET` | `/api/tags` | List all known tags from H2 |

### Implementation Steps
1. Create `Tag` JPA entity and `TagRepository` (H2 side)
2. Add `@ManyToMany` tags field to existing `Post` entity
3. Create `PostsByTagKey` and `PostsByTag` Cassandra entities
4. Create `PostsByTagRepository` (Cassandra side)
5. Create `TagService` that writes to both stores when tags change
6. Add endpoints to `ServerController`
7. Test: add tags to a post, then query by tag — verify Cassandra returns results
8. Bonus: add a tag cloud to the frontend

### Complexity
**Medium-High** — 6 new files, modifies existing `Post` model. Most educational about the dual-database pattern and denormalization trade-offs. Requires keeping two stores in sync.

---

## Comparison

| | Audit Log | View Tracker | Posts-by-Tag |
|---|-----------|-------------|--------------|
| **Complexity** | Low-Medium | Medium | Medium-High |
| **New files** | 3 | 6 | 6 |
| **Existing model changes** | None | None | `Post.java` modified |
| **Cassandra concepts** | Partition key, clustering key, append-only | Composite partition key, counter tables, time bucketing | Denormalization, dual-database sync, query-driven design |
| **Write pattern** | Every API call | Every page view | On tag change |
| **Read pattern** | By post + time range | By post + date | By tag name |
| **Risk to existing code** | Very low | Low | Medium (touches `Post` model) |
| **Best for learning** | Basics of Cassandra + Spring Data | Cassandra-specific features (counters, composite keys) | Real-world dual-DB architecture patterns |
