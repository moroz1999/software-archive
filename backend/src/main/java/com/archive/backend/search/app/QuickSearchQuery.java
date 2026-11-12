package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import org.jspecify.annotations.Nullable;

/** The keyword of the quick search, none for a blank one, and the platform of the switch. */
public record QuickSearchQuery(
        @Nullable CatalogKeyword keyword, @Nullable PlatformCode platform) {}
