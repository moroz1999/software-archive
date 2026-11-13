package com.archive.backend.user.read;

import java.util.Optional;
import java.util.UUID;

public interface UserReadRepository {

    /** A confirmed account only. */
    Optional<CurrentUser> findCurrent(UUID id);
}
