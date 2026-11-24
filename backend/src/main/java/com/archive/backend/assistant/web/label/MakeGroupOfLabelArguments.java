package com.archive.backend.assistant.web.label;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of make_group_of_label. */
public record MakeGroupOfLabelArguments(UUID id, @Nullable List<String> types) {}
