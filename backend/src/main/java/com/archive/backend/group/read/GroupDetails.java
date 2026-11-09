package com.archive.backend.group.read;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** One group with everything shown on its page. */
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
        List<RelatedGroup> groups
) {
}
