package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.Set;

public record CreateReleaseCommand(
        Actor actor, SoftwareId softwareId, ReleaseContent content, Set<HardwareCode> hardware) {}
