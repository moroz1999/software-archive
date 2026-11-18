package com.archive.backend.group.read;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Reads groups as plain read models (docs/backend.md).
 */
public interface GroupReadRepository {

    /**
     * A page of the catalog by name; each filter applies when given. The platform keeps only the owners credited on
     * software of that platform (docs/domain/hardware.md).
     */
    Page<GroupCatalogEntry> findCatalogPage(
            @Nullable String letter,
            @Nullable UUID countryId,
            @Nullable UUID cityId,
            @Nullable String platform,
            Pageable pageable);

    /**
     * A group, its locations named in the language of the given code, or in Russian when they have no name in that one;
     * nothing for an unknown group.
     */
    Optional<GroupDetails> findById(UUID id, String language);
}
