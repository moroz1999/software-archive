package com.archive.backend.search.domain;

/** A document of the search: what the search knows of one entity, under the entity's id. */
public sealed interface SearchDocument permits SoftwareDocument, AuthorDocument, GroupDocument {

    /** The entity the document is of. */
    SearchEntity entity();
}
