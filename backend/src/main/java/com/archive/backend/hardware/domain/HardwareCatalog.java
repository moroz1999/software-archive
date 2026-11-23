package com.archive.backend.hardware.domain;

import java.util.Set;

/** The reference book of platforms, of hardware and of its groups, asked by the factories of hardware sets and places. */
public interface HardwareCatalog {

    boolean platformExists(PlatformCode platform);

    /** Whether every platform is in the reference book; true for none. */
    boolean platformsExist(Set<PlatformCode> platforms);

    boolean groupExists(HardwareGroupCode group);

    /**
     * Whether every item is in the reference book and belongs to the platform; true for none.
     */
    boolean allOnPlatform(Set<HardwareCode> codes, PlatformCode platform);
}
