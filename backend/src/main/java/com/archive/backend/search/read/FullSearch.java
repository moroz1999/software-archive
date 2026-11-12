package com.archive.backend.search.read;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The full search of people and of groups, a section at a time, the best matches first. Fails with an unchecked
 * exception when the engine does not answer.
 */
public interface FullSearch {

    /** A page of the people the keyword matches, of the platform when one is given. */
    Page<FoundAuthor> authors(String keyword, @Nullable String platform, Pageable pageable);

    /** A page of the groups the keyword matches, of the platform when one is given. */
    Page<FoundGroup> groups(String keyword, @Nullable String platform, Pageable pageable);
}
