package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** The arguments of create_group. */
public record CreateGroupArguments(
        String name,
        @Nullable String notes,
        @Nullable List<String> types,
        @Nullable List<GroupLabelArguments> labels,
        @Nullable List<LocationArguments> locations,
        @Nullable List<MemberArguments> members) {}
