package com.archive.backend.hardware.domain;

import java.util.Set;

/**
 * The reference book of platforms and their hardware, asked by the factory of hardware sets.
 */
public interface HardwareCatalog {

    boolean platformExists(PlatformCode platform);

    /**
     * Whether every item is in the reference book and belongs to the platform; true for none.
     */
    boolean allOnPlatform(Set<HardwareCode> codes, PlatformCode platform);
}
