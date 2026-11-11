package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Categories something is in, all of the archive, never a category together with one above it: of a line only the
 * deepest category chosen stays (docs/domain/categories.md). Built from the lines of the categories, so the rule holds
 * whoever builds it; only {@link CategorySets} looks the lines up for new categories.
 */
@ValueObject
public final class CategorySet {

    /**
     * In no category.
     */
    public static final CategorySet EMPTY = new CategorySet(List.of());

    private final Set<CategoryLine> lines;

    CategorySet(Collection<CategoryLine> lines) {
        Objects.requireNonNull(lines, "lines");
        Set<CategoryId> above =
                lines.stream().flatMap(line -> line.ancestors().stream()).collect(Collectors.toSet());
        this.lines =
                lines.stream().filter(line -> !above.contains(line.category())).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Categories read back from storage, with their lines as the tree has them now: a category the tree has meanwhile
     * put above another of them leaves.
     */
    public static CategorySet restore(Collection<CategoryLine> lines) {
        return new CategorySet(lines);
    }

    public Set<CategoryId> ids() {
        return lines.stream().map(CategoryLine::category).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof CategorySet set && set.ids().equals(ids());
    }

    @Override
    public int hashCode() {
        return ids().hashCode();
    }

    @Override
    public String toString() {
        return "CategorySet" + ids();
    }
}
