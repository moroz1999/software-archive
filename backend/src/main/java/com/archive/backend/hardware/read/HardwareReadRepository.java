package com.archive.backend.hardware.read;

import java.util.Collection;
import java.util.List;

/**
 * Reads the hardware reference book as plain read models (docs/backend.md).
 */
public interface HardwareReadRepository {

    /**
     * The hardware of the platform, named as platforms are, by group in the order they are shown, then by that name;
     * none for an unknown one.
     */
    List<HardwareItem> findByPlatform(String platform, String language);

    /**
     * The hardware of the codes, named as platforms are, by group in the order they are shown, then by that name; a code
     * the reference book does not have is left out.
     */
    List<HardwareItem> findByCodes(Collection<String> codes, String language);
}
