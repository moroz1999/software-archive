package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;

public record GetSoftwareQuery(Actor actor, SoftwareId id) {}
