package com.archive.backend.label.read;

import java.util.Optional;
import java.util.UUID;

/** Labels as their pages show them. */
public interface LabelReadRepository {

    /**
     * A label, its locations named in the language of the given code, or in Russian when they have no name in that one.
     */
    Optional<LabelDetails> findById(UUID id, String language);
}
