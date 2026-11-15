package com.archive.backend.label.read;

import java.util.Optional;
import java.util.UUID;

/** Labels as their pages show them. */
public interface LabelReadRepository {

    Optional<LabelDetails> findById(UUID id);
}
