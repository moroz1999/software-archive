package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.SoftwareId;
import java.util.Set;

public record CreateReleaseCommand(SoftwareId softwareId, ReleaseContent content, Set<HardwareCode> hardware) {}
