package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of update_author: only the fields given change. */
public record UpdateAuthorArguments(
        UUID id,
        @Nullable String realName,
        @Nullable String notes,
        @Nullable List<AuthorLabelArguments> labels,
        @Nullable List<LocationArguments> locations) {}
