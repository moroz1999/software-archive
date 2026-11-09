package com.archive.backend.group.read;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/** Reads groups as plain read models (docs/backend.md). */
public interface GroupReadRepository {

    /** A page of the catalog by name; each filter applies when given. */
    Page<GroupCatalogEntry> findCatalogPage(@Nullable String letter, @Nullable UUID countryId, @Nullable UUID cityId, Pageable pageable);

    /** A group; nothing for an unknown one. */
    Optional<GroupDetails> findById(UUID id);
}
