package com.archive.backend.user.read;

import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserReadRepository {

    /**
     * A confirmed account only.
     */
    Optional<CurrentUser> findCurrent(UUID id);

    /**
     * Any account, confirmed or not.
     */
    Optional<UserEntry> findEntry(UUID id);

    /**
     * The accounts by their user name, whatever the letter case; with a search, those whose name or email holds it.
     */
    Page<UserEntry> findPage(@Nullable String search, Pageable pageable);
}
