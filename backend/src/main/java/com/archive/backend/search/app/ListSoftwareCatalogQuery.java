package com.archive.backend.search.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/** A page of the software catalog; every condition given narrows it, the platform being that of the switch. */
public record ListSoftwareCatalogQuery(
        @Nullable CatalogKeyword keyword,
        @Nullable PlatformCode platform,
        @Nullable ReleaseYear year,
        @Nullable CategoryId category,
        @Nullable CountryId country,
        Pageable pageable) {}
