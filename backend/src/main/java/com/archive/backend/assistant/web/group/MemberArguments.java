package com.archive.backend.assistant.web.group;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A member of a group: a person or a label of a person, with roles. */
public record MemberArguments(
        UUID id, String type, @Nullable List<String> roles) {}
