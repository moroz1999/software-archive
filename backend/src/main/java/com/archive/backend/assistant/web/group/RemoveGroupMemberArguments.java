package com.archive.backend.assistant.web.group;

import java.util.UUID;

/** The arguments of remove_group_member. */
public record RemoveGroupMemberArguments(UUID groupId, UUID id) {}
