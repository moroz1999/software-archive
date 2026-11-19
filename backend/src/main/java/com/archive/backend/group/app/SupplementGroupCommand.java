package com.archive.backend.group.app;

import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.user.domain.Actor;
import java.util.List;

/** What an import found of a group the archive has, to fill what it lacks. */
public record SupplementGroupCommand(Actor actor, GroupId id, List<LocationDraft> locations) {}
