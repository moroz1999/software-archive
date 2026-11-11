package com.archive.backend.search.app;

import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchIndex;
import com.archive.backend.search.domain.SoftwareDocument;
import com.archive.backend.search.domain.SoftwareDocuments;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds the search collections anew from the archive and puts them in use only once fully loaded
 * (docs/features/search.md#initial-load-and-rebuild). The worker waits meanwhile; the changes keep their tasks and it
 * applies them afterwards. A failed rebuild leaves the collections in use as they were.
 */
@Service
@RequiredArgsConstructor
public class RebuildSearchIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(RebuildSearchIndexHandler.class);
    private static final int BATCH = 500;

    private final SoftwareDocuments softwareDocuments;
    private final SearchIndex index;
    private final SearchWorkerPause pause;

    public void handle() {
        pause.run(this::rebuildSoftware);
    }

    private void rebuildSoftware() {
        SearchCollection fresh;
        try {
            fresh = index.create(SearchEntityType.SOFTWARE);
        } catch (RuntimeException e) {
            log.error("Search rebuild not started: no new collection", e);
            return;
        }
        try {
            load(fresh);
            index.switchTo(fresh);
            log.info("Search collection {} in use", fresh.name());
        } catch (RuntimeException e) {
            log.error("Search rebuild failed, the collections in use stay", e);
            dropQuietly(fresh);
        }
    }

    private void load(SearchCollection collection) {
        @Nullable UUID last = null;
        List<SoftwareDocument> batch;
        do {
            batch = softwareDocuments.after(last, BATCH);
            if (!batch.isEmpty()) {
                index.load(collection, batch);
                last = batch.getLast().id();
            }
        } while (batch.size() == BATCH);
    }

    private void dropQuietly(SearchCollection collection) {
        try {
            index.drop(collection);
        } catch (RuntimeException e) {
            log.warn("Search collection {} of a failed rebuild not dropped", collection.name(), e);
        }
    }
}
