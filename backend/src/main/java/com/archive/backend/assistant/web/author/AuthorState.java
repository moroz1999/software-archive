package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.LocationArguments;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** A person as they are, in the names of the arguments of their tools. */
public record AuthorState(
        @Nullable String realName,
        @Nullable String notes,
        List<AuthorLabelArguments> labels,
        List<LocationArguments> locations) {}
