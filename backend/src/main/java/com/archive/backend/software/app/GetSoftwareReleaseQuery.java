package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;

public record GetSoftwareReleaseQuery(SoftwareId softwareId, ReleaseId releaseId) {}
