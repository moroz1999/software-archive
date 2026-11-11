package com.archive.backend.group.app;

import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.domain.LabelDraft;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record UpdateGroupCommand(
        GroupId id,
        GroupName name,
        @Nullable GroupNotes notes,
        Set<GroupTypeCode> types,
        List<LabelDraft> labels,
        List<LocationDraft> locations) {}
