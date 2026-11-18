package com.archive.backend.hardware.app;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.Language;

public record ListHardwareQuery(PlatformCode platform, Language language) {}
