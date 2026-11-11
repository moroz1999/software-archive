package com.archive.backend.author.domain;

import java.util.Optional;

/**
 * Authors as domain entities, for the use cases that change them. Reads go through the read model.
 */
public interface AuthorRepository {

    Optional<Author> findById(AuthorId id);

    void save(Author author);

    /**
     * Deletes the person; their labels stay in the archive as labels of nobody, with their credits.
     */
    void delete(Author author);
}
