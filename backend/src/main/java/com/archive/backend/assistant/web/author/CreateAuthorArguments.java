package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** The arguments of create_author. */
public record CreateAuthorArguments(
        @Nullable String realName,
        @Nullable String notes,
        @Nullable List<AuthorLabelArguments> labels,
        @Nullable List<LocationArguments> locations) {}
