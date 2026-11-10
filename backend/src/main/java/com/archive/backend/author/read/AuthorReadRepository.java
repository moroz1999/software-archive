package com.archive.backend.author.read;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Reads authors as plain read models (docs/backend.md).
 */
public interface AuthorReadRepository {

    /**
     * A page of the catalog by name; each filter applies when given. The platform keeps only the owners credited on
     * software of that platform (docs/domain/hardware.md).
     */
    Page<AuthorCatalogEntry> findCatalogPage(
            @Nullable String letter,
            @Nullable UUID countryId,
            @Nullable UUID cityId,
            @Nullable String platform,
            Pageable pageable);

    /**
     * A person who has a name; nothing for an unknown or nameless one.
     */
    Optional<AuthorDetails> findById(UUID id);
}
