package com.archive.backend.hardware.read;

import java.util.List;

/**
 * Reads the hardware reference book as plain read models (docs/backend.md).
 */
public interface HardwareReadRepository {

    /**
     * The hardware of the platform, by group in the order they are shown, then by name; none for an unknown one.
     */
    List<HardwareItem> findByPlatform(String platform);
}
