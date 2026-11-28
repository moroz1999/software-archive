package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareLink;
import com.archive.backend.user.domain.Actor;
import java.util.List;

/** Adds links to software, each it does not have yet. */
public record AddSoftwareLinksCommand(Actor actor, SoftwareId softwareId, List<SoftwareLink> links) {}
