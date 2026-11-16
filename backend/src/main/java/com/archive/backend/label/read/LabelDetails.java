package com.archive.backend.label.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The page of a label: its name, its owner once it is found out, and the groups it was a member of under their main
 * names (docs/domain/authors.md#label).
 */
public record LabelDetails(
        UUID id, String name, @Nullable Owner owner, List<MemberOf> groups, boolean canChange, boolean canMakeOwner) {

    /**
     * The person ({@code author}) or the group the label belongs to: a person by the real name with the main name
     * next to it ({@code note}), or by the main name without a real name; a group by its name. {@code addedBy} is
     * whoever added the owner, {@code null} once their account was deleted.
     */
    public record Owner(
            String type,
            UUID id,
            String name,
            @Nullable String note,
            @Nullable UUID addedBy) {}

    public record MemberOf(UUID id, String name) {}

    /** The label with what the reader may do with it: attach it to an owner, and make a new owner of it. */
    public LabelDetails withRights(boolean canChange, boolean canMakeOwner) {
        return new LabelDetails(id, name, owner, groups, canChange, canMakeOwner);
    }
}
