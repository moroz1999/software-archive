package com.archive.backend.search.app;

import com.archive.backend.search.domain.SearchEntity;
import com.archive.backend.search.domain.SearchIndex;
import com.archive.backend.search.domain.SearchOutbox;
import com.archive.backend.search.domain.SearchTask;
import com.archive.backend.search.domain.SoftwareDocuments;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The search worker (docs/features/search.md#synchronization): brings the document of every due task up to date from
 * the archive as it is now, and removes the task only once the index confirmed it; a failed task waits for its retry.
 */
@Service
@RequiredArgsConstructor
public class ProcessSearchOutboxHandler {

    private static final Logger log = LoggerFactory.getLogger(ProcessSearchOutboxHandler.class);
    private static final int BATCH = 100;

    private final SearchOutbox outbox;
    private final SoftwareDocuments softwareDocuments;
    private final SearchIndex index;
    private final SearchWorkerPause pause;

    public void handle() {
        pause.run(() -> {
            List<SearchTask> due;
            do {
                due = outbox.due(BATCH);
                due.forEach(this::process);
            } while (due.size() == BATCH);
        });
    }

    private void process(SearchTask task) {
        try {
            bringUpToDate(task.entity());
            outbox.acknowledge(task);
        } catch (RuntimeException e) {
            log.warn("Search document of {} not brought up to date, retried later", task.entity(), e);
            outbox.retryLater(task);
        }
    }

    private void bringUpToDate(SearchEntity entity) {
        switch (entity.type()) {
            case SOFTWARE ->
                softwareDocuments.of(entity.id()).ifPresentOrElse(index::upsert, () -> index.delete(entity));
        }
    }
}
