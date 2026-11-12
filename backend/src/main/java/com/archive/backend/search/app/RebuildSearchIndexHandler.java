package com.archive.backend.search.app;

import com.archive.backend.search.domain.AuthorDocuments;
import com.archive.backend.search.domain.GroupDocuments;
import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchDocument;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchIndex;
import com.archive.backend.search.domain.SoftwareDocuments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds the search collections anew from the archive and puts them in use only once all of them are fully loaded
 * (docs/features/search.md#initial-load-and-rebuild). The worker waits meanwhile; the changes keep their tasks and it
 * applies them afterwards. A failed rebuild leaves the collections in use as they were.
 */
@Service
@RequiredArgsConstructor
public class RebuildSearchIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(RebuildSearchIndexHandler.class);
    private static final int BATCH = 500;

    private final SoftwareDocuments softwareDocuments;
    private final AuthorDocuments authorDocuments;
    private final GroupDocuments groupDocuments;
    private final SearchIndex index;
    private final SearchWorkerPause pause;

    /** The documents of a kind, a batch at a time after the id given. */
    private interface Batches {
        List<? extends SearchDocument> after(@Nullable UUID after, int limit);
    }

    public void handle() {
        pause.run(this::rebuild);
    }

    private Batches batchesOf(SearchEntityType type) {
        return switch (type) {
            case SOFTWARE -> softwareDocuments::after;
            case AUTHOR -> authorDocuments::after;
            case GROUP -> groupDocuments::after;
        };
    }

    private void rebuild() {
        List<SearchCollection> fresh = new ArrayList<>();
        try {
            for (SearchEntityType type : SearchEntityType.values()) {
                fresh.add(index.create(type));
            }
        } catch (RuntimeException e) {
            log.error("Search rebuild not started: no new collection", e);
            fresh.forEach(this::dropQuietly);
            return;
        }
        try {
            fresh.forEach(this::load);
            fresh.forEach(index::switchTo);
            log.info(
                    "Search collections {} in use",
                    fresh.stream().map(SearchCollection::name).toList());
        } catch (RuntimeException e) {
            log.error("Search rebuild failed, the collections in use stay", e);
            fresh.forEach(this::dropQuietly);
        }
    }

    private void load(SearchCollection collection) {
        Batches batches = batchesOf(collection.type());
        @Nullable UUID last = null;
        List<? extends SearchDocument> batch;
        do {
            batch = batches.after(last, BATCH);
            if (!batch.isEmpty()) {
                index.load(collection, batch);
                last = batch.getLast().entity().id();
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
