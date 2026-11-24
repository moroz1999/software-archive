package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of update_label: only the fields given change; the owner stays. */
public record UpdateLabelArguments(
        UUID id, @Nullable String name, @Nullable List<LocationArguments> locations) {}
