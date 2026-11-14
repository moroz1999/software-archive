package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What the search knows of software (docs/features/search.md#collections): its categories with all their ancestors,
 * the countries of the owners of its credited labels, and the screenshots and the names of who made it its card shows.
 */
@ValueObject
public record SoftwareDocument(
        UUID id,
        String title,
        @Nullable String releaseDate,
        List<String> alternativeTitles,
        String platform,
        @Nullable Integer year,
        List<UUID> categories,
        List<UUID> countries,
        List<UUID> screenshots,
        List<String> madeBy)
        implements SearchDocument {

    public SoftwareDocument(
            UUID id,
            String title,
            @Nullable String releaseDate,
            List<String> alternativeTitles,
            String platform,
            @Nullable Integer year,
            List<UUID> categories,
            List<UUID> countries) {
        this(id, title, releaseDate, alternativeTitles, platform, year, categories, countries, List.of(), List.of());
    }

    public SoftwareDocument(
            UUID id,
            String title,
            @Nullable String releaseDate,
            List<String> alternativeTitles,
            String platform,
            @Nullable Integer year,
            List<UUID> categories,
            List<UUID> countries,
            List<UUID> screenshots) {
        this(id, title, releaseDate, alternativeTitles, platform, year, categories, countries, screenshots, List.of());
    }

    public SoftwareDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(platform, "platform");
        alternativeTitles = List.copyOf(alternativeTitles);
        categories = List.copyOf(categories);
        countries = List.copyOf(countries);
        screenshots = List.copyOf(screenshots);
        madeBy = List.copyOf(madeBy);
    }

    @Override
    public SearchEntity entity() {
        return new SearchEntity(SearchEntityType.SOFTWARE, id);
    }
}
