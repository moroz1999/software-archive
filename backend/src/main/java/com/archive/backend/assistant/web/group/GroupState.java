package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** A group as it is, in the names of the arguments of its tools. */
public record GroupState(
        String name,
        @Nullable String notes,
        List<String> types,
        List<GroupLabelArguments> labels,
        List<LocationArguments> locations,
        List<MemberState> members) {}
