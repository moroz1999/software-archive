package com.archive.backend.importer.domain.basis;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A program as "Базис" shows it on its card.
 *
 * @param year the year as the card writes it; empty when the card tells none
 * @param categories the names of its categories there, such as {@code arcade} or {@code krista-2}
 * @param screenshotUrls the absolute addresses of its screenshots, in their order
 * @param authorIds the ids of its authors there, in their order
 * @param fileUrl the absolute address of its one file; a card may have none
 */
@ValueObject
public record BasisProgram(
        String id,
        String url,
        String title,
        String year,
        List<String> categories,
        @Nullable String description,
        List<String> screenshotUrls,
        List<String> authorIds,
        @Nullable String fileUrl) {

    public BasisProgram {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(year, "year");
        categories = List.copyOf(categories);
        screenshotUrls = List.copyOf(screenshotUrls);
        authorIds = List.copyOf(authorIds);
    }
}
