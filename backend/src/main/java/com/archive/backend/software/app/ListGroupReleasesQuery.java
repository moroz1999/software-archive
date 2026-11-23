package com.archive.backend.software.app;

import com.archive.backend.group.domain.GroupId;

/** The releases a group is credited on, for its page. */
public record ListGroupReleasesQuery(GroupId group) {}
