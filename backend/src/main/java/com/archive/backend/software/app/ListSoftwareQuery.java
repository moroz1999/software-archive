package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.PlatformCode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/** A page of software; only the software of the platform when one is given. */
public record ListSoftwareQuery(@Nullable PlatformCode platform, Pageable pageable) {}
