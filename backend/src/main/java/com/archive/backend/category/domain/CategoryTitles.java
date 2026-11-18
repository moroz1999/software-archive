package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Map;
import java.util.Optional;

/**
 * The titles of a category, one per language; the Russian one is always there (docs/domain/categories.md).
 */
@ValueObject
public record CategoryTitles(Map<Language, CategoryTitle> values) {

    public CategoryTitles {
        values = Map.copyOf(values);
        if (!values.containsKey(Language.RU)) {
            throw new InvalidValueException("title.required", "A category needs a title in Russian.");
        }
    }

    public Optional<CategoryTitle> in(Language language) {
        return Optional.ofNullable(values.get(language));
    }
}
