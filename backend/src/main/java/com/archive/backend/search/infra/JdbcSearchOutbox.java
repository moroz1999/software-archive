package com.archive.backend.search.infra;

import com.archive.backend.search.domain.Attempts;
import com.archive.backend.search.domain.SearchEntity;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchOutbox;
import com.archive.backend.search.domain.SearchTask;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The outbox in {@code search_outbox}, written in the transaction of the change that calls it. */
@Repository
@RequiredArgsConstructor
public class JdbcSearchOutbox implements SearchOutbox {

    private final JdbcClient jdbc;

    @Override
    public void enqueue(Collection<SearchEntity> entities) {
        entities.forEach(entity -> jdbc.sql("""
                        INSERT INTO search_outbox (entity_type, entity_id) VALUES (:type, :id)
                        ON CONFLICT (entity_type, entity_id) DO UPDATE
                        SET renewal = nextval('search_outbox_renewal'), attempts = 0, next_attempt_at = now()
                        """)
                .param("type", entity.type().name())
                .param("id", entity.id())
                .update());
    }

    @Override
    public List<SearchTask> due(int limit) {
        return jdbc.sql("""
                        SELECT entity_type, entity_id, renewal, attempts FROM search_outbox
                        WHERE next_attempt_at <= now()
                        ORDER BY next_attempt_at, renewal
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query((rs, n) -> new SearchTask(
                        new SearchEntity(
                                SearchEntityType.valueOf(rs.getString("entity_type")),
                                rs.getObject("entity_id", UUID.class)),
                        rs.getLong("renewal"),
                        new Attempts(rs.getInt("attempts"))))
                .list();
    }

    @Override
    public void acknowledge(SearchTask task) {
        jdbc.sql("""
                        DELETE FROM search_outbox
                        WHERE entity_type = :type AND entity_id = :id AND renewal = :renewal
                        """)
                .param("type", task.entity().type().name())
                .param("id", task.entity().id())
                .param("renewal", task.renewal())
                .update();
    }

    @Override
    public void retryLater(SearchTask task) {
        Attempts attempts = task.attempts().next();
        jdbc.sql("""
                        UPDATE search_outbox
                        SET attempts = :attempts, next_attempt_at = now() + make_interval(secs => :delay)
                        WHERE entity_type = :type AND entity_id = :id AND renewal = :renewal
                        """)
                .param("attempts", attempts.value())
                .param("delay", (double) attempts.retryDelay().toSeconds())
                .param("type", task.entity().type().name())
                .param("id", task.entity().id())
                .param("renewal", task.renewal())
                .update();
    }
}
