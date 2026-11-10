package com.archive.backend.hardware.domain;

import java.util.Set;

/**
 * Builds hardware sets, checking the platform and every item against the reference book.
 */
public class HardwareSets {

    private final HardwareCatalog catalog;

    public HardwareSets(HardwareCatalog catalog) {
        this.catalog = catalog;
    }

    public HardwareSet of(PlatformCode platform, Set<HardwareCode> codes) {
        if (!catalog.platformExists(platform)) {
            throw new PlatformUnknownException();
        }
        if (!catalog.allOnPlatform(codes, platform)) {
            throw new HardwareUnknownException();
        }
        return new HardwareSet(platform, codes);
    }
}
