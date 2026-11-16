package com.archive.backend.group.app;

import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.domain.GroupMemberDraft;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.domain.UnmarkedLabelDraft;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record CreateGroupCommand(
        Actor actor,
        GroupName name,
        @Nullable GroupNotes notes,
        Set<GroupTypeCode> types,
        List<UnmarkedLabelDraft> labels,
        List<LocationDraft> locations,
        List<GroupMemberDraft> members) {

    public CreateGroupCommand(
            Actor actor,
            GroupName name,
            @Nullable GroupNotes notes,
            Set<GroupTypeCode> types,
            List<UnmarkedLabelDraft> labels,
            List<LocationDraft> locations) {
        this(actor, name, notes, types, labels, locations, List.of());
    }
}
