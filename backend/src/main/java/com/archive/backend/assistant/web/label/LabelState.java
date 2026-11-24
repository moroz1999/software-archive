package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A label as it is, in the names of the arguments of its tools; whose it is stays as it is. */
public record LabelState(String name, @Nullable Owner owner, List<LocationArguments> locations) {

    /** The person ({@code author}) or the group it belongs to, by the name shown. */
    public record Owner(String type, UUID id, String name) {}
}
