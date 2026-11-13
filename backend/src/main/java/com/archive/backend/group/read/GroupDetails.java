package com.archive.backend.group.read;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * One group with everything shown on its page.
 */
public record GroupDetails(
        UUID id,
        String name,
        String groupName,
        @Nullable String notes,
        List<String> types,
        List<GroupLabel> labels,
        List<GroupLocation> locations,
        List<GroupMember> members,
        List<RelatedGroup> memberGroups,
        List<RelatedGroup> groups,
        @Nullable UUID addedBy,
        boolean canChange) {
    /** As read, before the handler tells whether the actor may change it. */
    public GroupDetails(
            UUID id,
            String name,
            String groupName,
            @Nullable String notes,
            List<String> types,
            List<GroupLabel> labels,
            List<GroupLocation> locations,
            List<GroupMember> members,
            List<RelatedGroup> memberGroups,
            List<RelatedGroup> groups) {
        this(id, name, groupName, notes, types, labels, locations, members, memberGroups, groups, null, false);
    }

    /** The same group, telling whether the actor may change it. */
    public GroupDetails withCanChange(boolean canChange) {
        return new GroupDetails(
                id,
                name,
                groupName,
                notes,
                types,
                labels,
                locations,
                members,
                memberGroups,
                groups,
                addedBy,
                canChange);
    }
}
