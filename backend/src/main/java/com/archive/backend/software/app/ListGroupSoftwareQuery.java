package com.archive.backend.software.app;

import com.archive.backend.group.domain.GroupId;

/** The software a group is credited on, for its page. */
public record ListGroupSoftwareQuery(GroupId group) {}
