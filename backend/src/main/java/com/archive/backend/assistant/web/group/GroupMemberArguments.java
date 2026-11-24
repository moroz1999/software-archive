package com.archive.backend.assistant.web.group;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of set_group_member. */
public record GroupMemberArguments(
        UUID groupId, UUID id, String type, @Nullable List<String> roles) {}
