package com.archive.backend.assistant.web.group;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A label of the group: one it has, by its id, or a new one, by its name. */
public record GroupLabelArguments(
        @Nullable UUID id, @Nullable String name) {}
