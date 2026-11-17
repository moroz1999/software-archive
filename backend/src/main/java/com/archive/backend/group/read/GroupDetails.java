package com.archive.backend.group.read;

import com.archive.backend.user.read.Addition;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One group with everything shown on its page, under its name.
 */
public record GroupDetails(
        UUID id,
        String name,
        @Nullable String notes,
        List<String> types,
        List<GroupLabel> labels,
        List<GroupLocation> locations,
        List<GroupMember> members,
        List<RelatedGroup> memberGroups,
        List<RelatedGroup> groups,
        Addition added,
        @Nullable UUID addedBy,
        boolean canChange) {
    /** As read, before the handler tells whether the actor may change it. */
    public GroupDetails(
            UUID id,
            String name,
            @Nullable String notes,
            List<String> types,
            List<GroupLabel> labels,
            List<GroupLocation> locations,
            List<GroupMember> members,
            List<RelatedGroup> memberGroups,
            List<RelatedGroup> groups,
            Addition added) {
        this(id, name, notes, types, labels, locations, members, memberGroups, groups, added, null, false);
    }

    /** The same group, telling whether the actor may change it. */
    public GroupDetails withCanChange(boolean canChange) {
        return new GroupDetails(
                id, name, notes, types, labels, locations, members, memberGroups, groups, added, addedBy, canChange);
    }
}
