package com.archive.backend.search.domain;

import java.util.Collection;
import java.util.List;

/**
 * The search documents waiting to be brought up to date, written in the transaction of the change
 * (docs/features/search.md#synchronization).
 */
public interface SearchOutbox {

    /** Adds a task for each entity, or renews the one waiting: due at once, attempts forgotten. */
    void enqueue(Collection<SearchEntity> entities);

    /** The tasks due now, at most {@code limit}. */
    List<SearchTask> due(int limit);

    /** Removes the task, unless a later change renewed it meanwhile. */
    void acknowledge(SearchTask task);

    /** Counts a failure of the task and puts it off by its retry delay. */
    void retryLater(SearchTask task);
}
