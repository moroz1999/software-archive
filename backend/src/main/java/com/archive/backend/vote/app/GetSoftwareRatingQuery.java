package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;

public record GetSoftwareRatingQuery(Actor actor, SoftwareId software) {}
