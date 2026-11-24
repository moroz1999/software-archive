package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of update_group: only the fields given change. */
public record UpdateGroupArguments(
        UUID id,
        @Nullable String name,
        @Nullable String notes,
        @Nullable List<String> types,
        @Nullable List<GroupLabelArguments> labels,
        @Nullable List<LocationArguments> locations,
        @Nullable List<MemberArguments> members) {}
