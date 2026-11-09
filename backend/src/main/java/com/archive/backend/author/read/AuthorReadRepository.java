package com.archive.backend.author.read;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/** Reads authors as plain read models (docs/backend.md). */
public interface AuthorReadRepository {

    /** A page of the catalog by name; each filter applies when given. */
    Page<AuthorCatalogEntry> findCatalogPage(@Nullable String letter, @Nullable UUID countryId, @Nullable UUID cityId, Pageable pageable);

    /** A person who has a name; nothing for an unknown or nameless one. */
    Optional<AuthorDetails> findById(UUID id);
}
