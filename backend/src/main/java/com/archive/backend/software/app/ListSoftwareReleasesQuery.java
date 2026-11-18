package com.archive.backend.software.app;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;

public record ListSoftwareReleasesQuery(Actor actor, SoftwareId id, Language language) {}
