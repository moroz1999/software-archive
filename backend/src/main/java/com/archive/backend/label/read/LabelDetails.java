package com.archive.backend.label.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The page of a label: its name, its owner once it is found out, and the groups it was a member of under their main
 * names (docs/domain/authors.md#label).
 */
public record LabelDetails(UUID id, String name, @Nullable Owner owner, List<MemberOf> groups, boolean canChange) {

    /**
     * The person ({@code author}) or the group the label belongs to: a person by the real name with the main name
     * next to it ({@code note}), or by the main name without a real name; a group by its name.
     */
    public record Owner(
            String type, UUID id, String name, @Nullable String note) {}

    public record MemberOf(UUID id, String name) {}

    public LabelDetails withCanChange(boolean canChange) {
        return new LabelDetails(id, name, owner, groups, canChange);
    }
}
