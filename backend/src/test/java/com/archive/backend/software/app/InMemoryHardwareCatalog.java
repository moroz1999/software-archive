package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCatalog;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Knows only the platforms it was given, each with only the hardware it was given. */
class InMemoryHardwareCatalog implements HardwareCatalog {

    private final Map<PlatformCode, Set<HardwareCode>> onPlatform = new HashMap<>();

    InMemoryHardwareCatalog on(PlatformCode platform, HardwareCode... codes) {
        onPlatform.computeIfAbsent(platform, key -> new HashSet<>()).addAll(Set.of(codes));
        return this;
    }

    @Override
    public boolean platformExists(PlatformCode platform) {
        return onPlatform.containsKey(platform);
    }

    @Override
    public boolean allOnPlatform(Set<HardwareCode> codes, PlatformCode platform) {
        return onPlatform.getOrDefault(platform, Set.of()).containsAll(codes);
    }
}
