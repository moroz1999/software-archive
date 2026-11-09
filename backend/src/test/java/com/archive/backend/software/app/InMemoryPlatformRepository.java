package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.domain.PlatformRepository;
import java.util.Set;

/** Knows only the platforms it was given. */
class InMemoryPlatformRepository implements PlatformRepository {

    private final Set<PlatformCode> known;

    InMemoryPlatformRepository(PlatformCode... known) {
        this.known = Set.of(known);
    }

    @Override
    public boolean exists(PlatformCode code) {
        return known.contains(code);
    }
}
