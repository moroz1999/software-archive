package com.archive.backend.search.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.CatalogOrder;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.shared.domain.Language;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * A page of the software catalog; every condition given narrows it, the platform being that of the switch and the
 * platforms those chosen in the filter; the hardware offered is named in the language.
 */
public record ListSoftwareCatalogQuery(
        @Nullable CatalogKeyword keyword,
        @Nullable PlatformCode platform,
        @Nullable ReleaseYear year,
        @Nullable CategoryId category,
        @Nullable CountryId country,
        @Nullable CityId city,
        List<PlatformCode> platforms,
        List<HardwareCode> hardware,
        Language language,
        CatalogOrder order,
        Pageable pageable) {

    /** The software of a page of the catalog, on the site and to the assistant alike. */
    public static final int PAGE_SIZE = 30;

    public ListSoftwareCatalogQuery {
        platforms = List.copyOf(platforms);
        hardware = List.copyOf(hardware);
    }

    /** Sorted by title. */
    public ListSoftwareCatalogQuery(
            @Nullable CatalogKeyword keyword,
            @Nullable PlatformCode platform,
            @Nullable ReleaseYear year,
            @Nullable CategoryId category,
            @Nullable CountryId country,
            @Nullable CityId city,
            List<PlatformCode> platforms,
            List<HardwareCode> hardware,
            Language language,
            Pageable pageable) {
        this(
                keyword,
                platform,
                year,
                category,
                country,
                city,
                platforms,
                hardware,
                language,
                CatalogOrder.TITLE,
                pageable);
    }

    public ListSoftwareCatalogQuery(
            @Nullable CatalogKeyword keyword,
            @Nullable PlatformCode platform,
            @Nullable ReleaseYear year,
            @Nullable CategoryId category,
            @Nullable CountryId country,
            Pageable pageable) {
        this(
                keyword,
                platform,
                year,
                category,
                country,
                null,
                List.of(),
                List.of(),
                Language.EN,
                CatalogOrder.TITLE,
                pageable);
    }
}
