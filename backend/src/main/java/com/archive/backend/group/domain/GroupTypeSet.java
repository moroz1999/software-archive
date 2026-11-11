package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The types of a group: at least one, all of the reference book. Only {@link GroupTypes} builds one, checking it;
 * storage restores what was checked.
 */
@ValueObject
public final class GroupTypeSet {

    private final Set<GroupTypeCode> codes;

    GroupTypeSet(Set<GroupTypeCode> codes) {
        this.codes = Set.copyOf(Objects.requireNonNull(codes, "codes"));
    }

    /**
     * Types read back from storage: they were checked when stored.
     */
    public static GroupTypeSet restore(Set<GroupTypeCode> codes) {
        return new GroupTypeSet(codes);
    }

    public Set<GroupTypeCode> codes() {
        return codes;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof GroupTypeSet set && set.codes.equals(codes);
    }

    @Override
    public int hashCode() {
        return codes.hashCode();
    }

    @Override
    public String toString() {
        return "GroupTypeSet" + codes;
    }
}
