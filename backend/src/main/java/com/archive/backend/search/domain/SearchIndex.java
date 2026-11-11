package com.archive.backend.search.domain;

import java.util.List;

/**
 * The search engine's documents (docs/features/search.md). Every call fails with an unchecked exception when the engine
 * does not confirm it.
 */
public interface SearchIndex {

    /** Puts the document in the collection in use, replacing the one of the same id. */
    void upsert(SoftwareDocument document);

    /** Removes the entity's document from the collection in use; a missing document counts as removed. */
    void delete(SearchEntity entity);

    /** Whether a collection of the kind is in use. */
    boolean exists(SearchEntityType type);

    /** Creates a new, empty collection of the kind, not yet in use. */
    SearchCollection create(SearchEntityType type);

    /** Loads documents into a collection not yet in use; fails unless every one of them was taken. */
    void load(SearchCollection collection, List<SoftwareDocument> documents);

    /** Puts the collection in use and drops the one used before. */
    void switchTo(SearchCollection collection);

    /** Drops a collection not in use. */
    void drop(SearchCollection collection);
}
