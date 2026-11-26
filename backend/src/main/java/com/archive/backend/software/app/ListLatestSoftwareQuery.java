package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.PlatformCode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * The most recently added software, only of the platform when one is given; only the page size of the pageable
 * counts.
 */
public record ListLatestSoftwareQuery(@Nullable PlatformCode platform, Pageable pageable) {

    /** The software the home page shows, and the assistant reads as a page. */
    public static final int PAGE_SIZE = 12;
}
