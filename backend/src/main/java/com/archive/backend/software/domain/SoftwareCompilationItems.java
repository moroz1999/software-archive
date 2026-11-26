package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** The archive programs that make up a compilation. */
@ValueObject
public final class SoftwareCompilationItems {

    private final SoftwareId compilation;
    private final Set<SoftwareId> ids;

    SoftwareCompilationItems(SoftwareId compilation, Set<SoftwareId> ids) {
        this.compilation = Objects.requireNonNull(compilation);
        this.ids = Set.copyOf(ids);
    }

    public static SoftwareCompilationItems restore(SoftwareId compilation, Set<SoftwareId> ids) {
        return new SoftwareCompilationItems(compilation, ids);
    }

    public Set<SoftwareId> ids() {
        return ids;
    }

    void requireOwner(SoftwareId owner) {
        if (!compilation.equals(owner)) {
            throw new IllegalArgumentException("The programs were checked for another compilation.");
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof SoftwareCompilationItems items
                && compilation.equals(items.compilation)
                && ids.equals(items.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compilation, ids);
    }
}
