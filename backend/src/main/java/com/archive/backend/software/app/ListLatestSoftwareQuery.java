package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.PlatformCode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * The most recently added software, only of the platform when one is given; only the page size of the pageable
 * counts.
 */
public record ListLatestSoftwareQuery(@Nullable PlatformCode platform, Pageable pageable) {}
