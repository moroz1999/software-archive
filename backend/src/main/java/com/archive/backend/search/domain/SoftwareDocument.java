package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What the search knows of software (docs/features/search.md#collections): its categories with all their ancestors,
 * and the countries of the owners of its credited labels.
 */
@ValueObject
public record SoftwareDocument(
        UUID id,
        String title,
        @Nullable LocalDate releaseDate,
        List<String> alternativeTitles,
        String platform,
        @Nullable Integer year,
        List<UUID> categories,
        List<UUID> countries)
        implements SearchDocument {

    public SoftwareDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(platform, "platform");
        alternativeTitles = List.copyOf(alternativeTitles);
        categories = List.copyOf(categories);
        countries = List.copyOf(countries);
    }

    @Override
    public SearchEntity entity() {
        return new SearchEntity(SearchEntityType.SOFTWARE, id);
    }
}
