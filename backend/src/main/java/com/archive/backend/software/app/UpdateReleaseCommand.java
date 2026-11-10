package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import java.util.Set;

public record UpdateReleaseCommand(
        SoftwareId softwareId, ReleaseId releaseId, ReleaseContent content, Set<HardwareCode> hardware) {}
