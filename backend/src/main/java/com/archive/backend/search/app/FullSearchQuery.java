package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/** A page of a section of the full search: the keyword, none for a blank one, and the platform of the switch. */
public record FullSearchQuery(
        @Nullable CatalogKeyword keyword, @Nullable PlatformCode platform, Pageable pageable) {}
