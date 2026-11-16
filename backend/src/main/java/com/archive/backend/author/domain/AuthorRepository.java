package com.archive.backend.author.domain;

import com.archive.backend.label.domain.LabelId;
import java.util.Optional;

/**
 * Authors as domain entities, for the use cases that change them. Reads go through the read model.
 */
public interface AuthorRepository {

    Optional<Author> findById(AuthorId id);

    /** The person a label belongs to, if it belongs to one. */
    Optional<Author> findByLabel(LabelId label);

    void save(Author author);

    /**
     * Deletes the person; their labels stay in the archive as labels of nobody, with their credits.
     */
    void delete(Author author);
}
