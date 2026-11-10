package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;

public record DeleteReleaseCommand(SoftwareId softwareId, ReleaseId releaseId) {}
