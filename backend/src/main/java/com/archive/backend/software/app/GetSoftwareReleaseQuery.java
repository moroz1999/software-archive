package com.archive.backend.software.app;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;

public record GetSoftwareReleaseQuery(SoftwareId softwareId, ReleaseId releaseId, Language language) {}
