package com.archive.backend.search.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.search.domain.Attempts;
import com.archive.backend.search.domain.SearchEntity;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchTask;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** The outbox of search tasks (docs/features/search.md#synchronization). */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcSearchOutbox.class})
class JdbcSearchOutboxIT {

    @Autowired
    private JdbcSearchOutbox outbox;

    @Autowired
    private JdbcTemplate jdbc;

    private static SearchEntity software() {
        return new SearchEntity(SearchEntityType.SOFTWARE, UUID.randomUUID());
    }

    private SearchTask onlyDue() {
        List<SearchTask> due = outbox.due(100);
        assertThat(due).hasSize(1);
        return due.getFirst();
    }

    private List<SearchEntity> dueEntities() {
        return outbox.due(100).stream().map(SearchTask::entity).toList();
    }

    @Test
    void keepsOneTaskPerEntity() {
        SearchEntity elite = software();

        outbox.enqueue(List.of(elite));
        outbox.enqueue(List.of(elite));

        assertThat(dueEntities()).containsExactly(elite);
    }

    @Test
    void acknowledgingRemovesTheTask() {
        SearchEntity elite = software();
        outbox.enqueue(List.of(elite));

        outbox.acknowledge(onlyDue());

        assertThat(dueEntities()).isEmpty();
    }

    @Test
    void keepsATaskRenewedWhileItWasProcessed() {
        SearchEntity elite = software();
        outbox.enqueue(List.of(elite));
        SearchTask taken = onlyDue();

        outbox.enqueue(List.of(elite));
        outbox.acknowledge(taken);

        assertThat(dueEntities()).containsExactly(elite);
    }

    @Test
    void putsAFailedTaskOffAndCountsTheFailure() {
        SearchEntity elite = software();
        outbox.enqueue(List.of(elite));

        outbox.retryLater(onlyDue());

        assertThat(dueEntities()).isEmpty();
        jdbc.update("UPDATE search_outbox SET next_attempt_at = now() WHERE entity_id = ?", elite.id());
        assertThat(outbox.due(100))
                .singleElement()
                .extracting(SearchTask::attempts)
                .isEqualTo(new Attempts(1));
    }

    @Test
    void aNewChangeMakesAPutOffTaskDueAtOnce() {
        SearchEntity elite = software();
        outbox.enqueue(List.of(elite));
        outbox.retryLater(onlyDue());

        outbox.enqueue(List.of(elite));

        assertThat(outbox.due(100))
                .singleElement()
                .extracting(SearchTask::attempts)
                .isEqualTo(new Attempts(0));
    }

    @Test
    void givesAtMostTheLimit() {
        outbox.enqueue(List.of(software(), software(), software()));

        assertThat(outbox.due(2)).hasSize(2);
    }
}
