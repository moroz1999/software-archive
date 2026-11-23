package com.archive.backend.hardware.domain;

/** Builds placements of hardware, checking the group and the platforms against the reference book. */
public class HardwarePlacements {

    private final HardwareCatalog catalog;

    public HardwarePlacements(HardwareCatalog catalog) {
        this.catalog = catalog;
    }

    public HardwarePlacement of(HardwareGroupCode group, HardwarePlatforms platforms) {
        if (!catalog.groupExists(group)) {
            throw new HardwareGroupUnknownException();
        }
        if (!catalog.platformsExist(platforms.values())) {
            throw new HardwarePlatformUnknownException();
        }
        return new HardwarePlacement(group, platforms);
    }
}
